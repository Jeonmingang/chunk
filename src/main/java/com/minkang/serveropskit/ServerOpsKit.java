
package com.minkang.serveropskit;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ServerOpsKit extends JavaPlugin implements Listener {

    private ZoneId zone;
    private String webhookUrl;
    private String webhookName;
    private String webhookAvatar;

    // pregen throttle
    private boolean pregenEnabled;
    private int pregenWindowSec;
    private double pregenSpikeMs;
    private double pregenResumeTps;
    private int pregenMaxOnline;
    private boolean pregenAnnounce;
    private boolean pregenQuiet;
    private Map<String, CenterRadius> centerRadiusMap = new HashMap<>();

    // spark
    private boolean sparkEnabled;
    private double sparkBelowTps;
    private int sparkDurationSec;
    private int sparkTimeoutSec;

    // entity relief
    private boolean reliefEnabled;
    private int reliefSampleTicks;
    private int itemsThreshold;
    private int mobsThreshold;
    private boolean reliefMessagePlayers;
    private List<String> reliefExtraCmds;
    private String itemCleanupMode; // safe | vanilla_kill
    private int itemAgeSecondsMin;
    private Set<String> whitelistIds = new HashSet<>();
    private List<String> whitelistPrefixes = new ArrayList<>();

    // backup
    private boolean backupEnabled;
    private LocalTime backupTime;
    private List<String> backupWorlds;
    private File backupDir;
    private int backupKeepMax;

    // autorestart
    private boolean restartEnabled;
    private int restartCheckEvery;
    private int heapThreshold;
    private int sustainSeconds;
    private List<Integer> warnMinutes;
    private String restartCommand;

    // logwatch
    private boolean logwatchEnabled;
    private File latestLog;

    private RollingWindowTick rolling = new RollingWindowTick();
    private long lowTpsStart = 0L;
    private long lastBackupDay = -1;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfigValues();

        Bukkit.getPluginManager().registerEvents(this, this);
        rolling.start(this);

        Bukkit.getScheduler().runTaskTimer(this, () -> {
            handlePregenThrottle();
            handleSparkTrigger();
        }, 20L, 20L);

        if (reliefEnabled) {
            Bukkit.getScheduler().runTaskTimer(this, this::handleEntityRelief, reliefSampleTicks, reliefSampleTicks);
        }
        if (backupEnabled) {
            Bukkit.getScheduler().runTaskTimer(this, this::handleBackupSchedule, 200L, 200L);
        }
        if (restartEnabled) {
            Bukkit.getScheduler().runTaskTimer(this, this::handleAutoRestart, 20L * restartCheckEvery, 20L * restartCheckEvery);
        }
        if (logwatchEnabled) {
            Bukkit.getScheduler().runTaskTimerAsynchronously(this, this::handleLogWatch, 40L, 40L);
        }

        getLogger().info("ServerOpsKit v" + getDescription().getVersion() + " enabled.");
    }

    private void loadConfigValues() {
        zone = ZoneId.of(getConfig().getString("timezone", "Asia/Seoul"));
        webhookUrl = getConfig().getString("discord.webhook_url", "");
        webhookName = getConfig().getString("discord.username", "ServerOpsKit");
        webhookAvatar = getConfig().getString("discord.avatar_url", "");

        pregenEnabled = getConfig().getBoolean("pregen_throttle.enabled", true);
        pregenWindowSec = getConfig().getInt("pregen_throttle.avg_window_seconds", 5);
        pregenSpikeMs = getConfig().getDouble("pregen_throttle.spike_ms", 60);
        pregenResumeTps = getConfig().getDouble("pregen_throttle.resume_tps", 18.5);
        pregenMaxOnline = getConfig().getInt("pregen_throttle.max_online_to_run", 2);
        pregenAnnounce = getConfig().getBoolean("pregen_throttle.announce_to_players", true);
        pregenQuiet = getConfig().getBoolean("pregen_throttle.use_chunky_quiet", true);

        if (getConfig().isConfigurationSection("pregen_throttle.worlds_center_radius")) {
            for (String w : getConfig().getConfigurationSection("pregen_throttle.worlds_center_radius").getKeys(false)) {
                String v = getConfig().getString("pregen_throttle.worlds_center_radius." + w, "0 0 3000");
                String[] parts = v.split("\\s+");
                double cx = 0, cz = 0; int r = 3000;
                try {
                    cx = Double.parseDouble(parts[0]);
                    cz = Double.parseDouble(parts[1]);
                    r = Integer.parseInt(parts[2]);
                } catch (Exception ignored) {}
                centerRadiusMap.put(w, new CenterRadius(cx, cz, r));
            }
        }

        sparkEnabled = getConfig().getBoolean("spark_trigger.enabled", true);
        sparkBelowTps = getConfig().getDouble("spark_trigger.below_tps", 15.0);
        sparkDurationSec = getConfig().getInt("spark_trigger.duration_seconds", 10);
        sparkTimeoutSec = getConfig().getInt("spark_trigger.timeout_seconds", 60);

        reliefEnabled = getConfig().getBoolean("entity_relief.enabled", true);
        reliefSampleTicks = getConfig().getInt("entity_relief.sample_every_ticks", 100);
        itemsThreshold = getConfig().getInt("entity_relief.thresholds.items_total", 1200);
        mobsThreshold = getConfig().getInt("entity_relief.thresholds.mobs_total", 900);
        reliefMessagePlayers = getConfig().getBoolean("entity_relief.actions.message_players", true);
        reliefExtraCmds = (List<String>) getConfig().getList("entity_relief.actions.extra_commands", new ArrayList<>());
        itemCleanupMode = getConfig().getString("entity_relief.item_cleanup.mode","safe");
        itemAgeSecondsMin = getConfig().getInt("entity_relief.item_cleanup.age_seconds_min", 120);
        whitelistIds.clear();
        whitelistIds.addAll(getConfig().getStringList("entity_relief.item_cleanup.whitelist_ids"));
        whitelistPrefixes = getConfig().getStringList("entity_relief.item_cleanup.whitelist_prefixes");

        backupEnabled = getConfig().getBoolean("backup.enabled", true);
        String hm = getConfig().getString("backup.hour_minute", "04:30");
        try { backupTime = LocalTime.parse(hm); } catch (Exception e) { backupTime = LocalTime.of(4,30); }
        backupWorlds = (List<String>) getConfig().getList("backup.worlds", Arrays.asList("world","world_nether","world_the_end"));
        backupDir = new File(getDataFolder().getParentFile().getParentFile(), getConfig().getString("backup.dir", "backups"));
        if (!backupDir.exists()) backupDir.mkdirs();
        backupKeepMax = getConfig().getInt("backup.keep_max", 14);

        restartEnabled = getConfig().getBoolean("autorestart.enabled", true);
        restartCheckEvery = getConfig().getInt("autorestart.check_every_seconds", 30);
        heapThreshold = getConfig().getInt("autorestart.heap_usage_percent_threshold", 90);
        sustainSeconds = getConfig().getInt("autorestart.sustain_seconds", 180);
        warnMinutes = (List<Integer>) getConfig().getList("autorestart.warn_minutes", Arrays.asList(5,1));
        restartCommand = getConfig().getString("autorestart.restart_command", "restart");

        logwatchEnabled = getConfig().getBoolean("logwatch.enabled", true);
        latestLog = new File(getDataFolder().getParentFile().getParentFile(), getConfig().getString("logwatch.latest_log_path", "logs/latest.log"));
    }

    // ===== Metrics =====
    private static class RollingWindowTick {
        private final int windowSize = 100; // ~5초
        private final Deque<Long> tickNs = new ArrayDeque<>();
        private long last = System.nanoTime();
        double avgTickMs = 0.0;
        double estTps = 20.0;
        void start(JavaPlugin plugin){
            Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                long now = System.nanoTime();
                long dt = now - last; last = now;
                tickNs.addLast(dt);
                while (tickNs.size() > windowSize) tickNs.removeFirst();
                long sum = 0L;
                for (long x : tickNs) sum += x;
                double avgNs = (tickNs.isEmpty()? 50_000_000.0 : (double)sum / tickNs.size());
                avgTickMs = avgNs / 1_000_000.0;
                estTps = Math.min(20.0, 1000.0 / Math.max(1.0, avgTickMs));
            }, 1L, 1L);
        }
    }

    private void sendDiscord(String title, String content) {
        if (webhookUrl == null || webhookUrl.isEmpty()) return;
        try {
            URL url = new URL(webhookUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setRequestProperty("Content-Type", "application/json");
            String json = "{\"username\":\""+escape(webhookName)+"\",\"avatar_url\":\""+escape(webhookAvatar)+"\"," +
                    "\"embeds\":[{\"title\":\""+escape(title)+"\",\"description\":\""+escape(content)+"\"}]}";
            try (OutputStream os = con.getOutputStream()) {
                os.write(json.getBytes(StandardCharsets.UTF_8));
            }
            con.getInputStream().close();
        } catch (Exception e) {
            getLogger().warning("Discord webhook error: " + e.getMessage());
        }
    }
    private String escape(String s){ if(s==null) return ""; return s.replace("\\","\\\\").replace("\"","\\\""); }

    private void handlePregenThrottle() {
        if (!pregenEnabled) return;
        double ms = rolling.avgTickMs;
        int online = Bukkit.getOnlinePlayers().size();

        if (ms >= pregenSpikeMs) {
            if (pregenAnnounce) Bukkit.broadcastMessage(ChatColor.YELLOW + "[프리젠 쓰로틀] 지연 스파이크 감지(" + String.format(java.util.Locale.ROOT,"%.1f",ms) + "ms) → 일시정지");
            if (pregenQuiet) dispatch("chunky quiet true");
            dispatch("chunky pause");
            sendDiscord("프리젠 일시정지","지연 스파이크 감지: 평균 tick " + String.format(java.util.Locale.ROOT,"%.1f",ms) + "ms\n온라인: " + online);
        } else {
            if (rolling.estTps >= pregenResumeTps && online <= pregenMaxOnline) {
                if (pregenQuiet) dispatch("chunky quiet true");
                dispatch("chunky continue");
            }
        }
    }

    private void handleSparkTrigger() {
        if (!sparkEnabled) return;
        double tps = rolling.estTps;
        long now = System.currentTimeMillis();
        if (tps < sparkBelowTps) {
            if (lowTpsStart == 0L) lowTpsStart = now;
            long elapsed = (now - lowTpsStart) / 1000L;
            if (elapsed >= sparkDurationSec) {
                dispatch("spark profiler --timeout " + sparkTimeoutSec);
                sendDiscord("spark profiler 트리거","TPS 하락 지속(" + String.format(java.util.Locale.ROOT,"%.1f",tps) + " < " + sparkBelowTps + " for " + elapsed + "s)");
                lowTpsStart = 0L;
            }
        } else {
            lowTpsStart = 0L;
        }
    }

    private void handleEntityRelief() {
        int items = 0, mobs = 0, removed = 0;
        for (World w : Bukkit.getWorlds()) {
            for (Entity e : w.getEntities()) {
                if (e instanceof Item) items++;
                else if (e instanceof LivingEntity) mobs++;
            }
        }
        if (items > itemsThreshold || mobs > mobsThreshold) {
            if (reliefMessagePlayers) Bukkit.broadcastMessage(ChatColor.RED + "[부하 완화] 아이템/몬스터가 많아 일부 정리를 진행합니다.");
            if ("vanilla_kill".equalsIgnoreCase(itemCleanupMode)) {
                dispatch("minecraft:kill @e[type=item]");
            } else {
                long now = System.currentTimeMillis();
                for (World w : Bukkit.getWorlds()) {
                    for (Entity e : new ArrayList<>(w.getEntitiesByClass(Item.class))) {
                        Item it = (Item)e;
                        ItemStack st = it.getItemStack();
                        // Spigot 1.16.5 호환: getType().getKey() 미사용, enum 이름을 id로 사용
                        String id = "minecraft:" + st.getType().name().toLowerCase(java.util.Locale.ROOT);
                        if (whitelistIds.contains(id)) continue;
                        boolean prefixed = false;
                        for (String p : whitelistPrefixes) { if (id.startsWith(p)) { prefixed = true; break; } }
                        if (prefixed) continue;
                        int ticksLived = it.getTicksLived();
                        long ageMs = ticksLived * 50L;
                        if (ageMs >= itemAgeSecondsMin * 1000L) {
                            it.remove();
                            removed++;
                        }
                    }
                }
            }
            for (String cmd : reliefExtraCmds) dispatch(cmd);
            sendDiscord("엔티티 부하 완화 실행", "items=" + items + ", mobs=" + mobs + ", removed(items)=" + removed);
        }
    }

    private void handleBackupSchedule() {
        LocalDate today = LocalDate.now(zone);
        int day = today.getDayOfYear();
        LocalTime now = LocalTime.now(zone);
        if (day != lastBackupDay && now.getHour() == backupTime.getHour() && now.getMinute() == backupTime.getMinute()) {
            lastBackupDay = day;
            try { runBackup(); } catch (Exception e) { getLogger().warning("백업 실패: " + e.getMessage()); }
        }
    }

    private void runBackup() throws IOException {
        dispatch("save-off");
        dispatch("save-all");
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File out = new File(backupDir, "backup_" + timestamp + ".zip");
        zipWorlds(out, backupWorlds);
        dispatch("save-on");
        sendDiscord("백업 완료", "파일: " + out.getName());
        File[] files = backupDir.listFiles((d, n) -> n.endsWith(".zip"));
        if (files != null && files.length > backupKeepMax) {
            Arrays.sort(files, Comparator.comparingLong(File::lastModified));
            for (int i = 0; i < files.length - backupKeepMax; i++) files[i].delete();
        }
    }

    private void zipWorlds(File zipFile, List<String> worlds) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            for (String wname : worlds) {
                File wdir = new File(getDataFolder().getParentFile().getParentFile(), wname);
                if (!wdir.exists()) continue;
                zipDir(zos, wdir, wdir.getParentFile());
            }
        }
    }
    private void zipDir(ZipOutputStream zos, File src, File base) throws IOException {
        if (src.isDirectory()) {
            File[] list = src.listFiles();
            if (list != null) for (File f : list) zipDir(zos, f, base);
        } else {
            String entryName = base.toPath().relativize(src.toPath()).toString().replace("\\","/");
            try (FileInputStream fis = new FileInputStream(src)) {
                zos.putNextEntry(new ZipEntry(entryName));
                byte[] buf = new byte[8192];
                int r;
                while ((r = fis.read(buf)) != -1) zos.write(buf, 0, r);
                zos.closeEntry();
            }
        }
    }

    private void handleAutoRestart() {
        long used = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        long max = Runtime.getRuntime().maxMemory();
        int pct = (int) Math.round((used * 100.0) / Math.max(1L, max));
        if (pct >= heapThreshold) {
            int ticks = sustainSeconds / restartCheckEvery;
            sustainCounter.offer(Boolean.TRUE);
            while (sustainCounter.size() > ticks) sustainCounter.poll();
            if (sustainCounter.size() == ticks) {
                for (int m : warnMinutes) {
                    Bukkit.getScheduler().runTaskLater(this, () -> Bukkit.broadcastMessage(ChatColor.GOLD + "[자동 재시작] " + m + "분 후 재시작합니다."), 0L);
                }
                Bukkit.getScheduler().runTaskLater(this, () -> {
                    sendDiscord("자동 재시작", "힙 사용률 " + pct + "%, 재시작 명령 실행");
                    dispatch(restartCommand);
                }, warnMinutes.get(warnMinutes.size()-1) * 60L * 20L);
                sustainCounter.clear();
            }
        } else {
            sustainCounter.clear();
        }
    }
    private final Queue<Boolean> sustainCounter = new ConcurrentLinkedQueue<>();

    private void handleLogWatch() {
        if (!latestLog.exists()) return;
        try (RandomAccessFile raf = new RandomAccessFile(latestLog, "r")) {
            long len = raf.length();
            long start = Math.max(0, len - 20000);
            raf.seek(start);
            String line;
            while ((line = raf.readLine()) != null) {
                String s = new String(line.getBytes("ISO-8859-1"), StandardCharsets.UTF_8);
                if (s.contains("The server has stopped responding") || s.contains("서버가 응답하지 않는다")) {
                    sendDiscord("Watchdog 감지", "로그에서 서버 정지 감지: latest.log");
                    break;
                }
            }
        } catch (Exception ignored) {}
    }

    private void dispatch(String cmd) { Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd); }

    @EventHandler
    public void onMove(PlayerMoveEvent e){
        if (centerRadiusMap.isEmpty()) return;
        World w = e.getPlayer().getWorld();
        CenterRadius cr = centerRadiusMap.get(w.getName());
        if (cr == null) return;
        double dx = e.getPlayer().getLocation().getX() - cr.cx;
        double dz = e.getPlayer().getLocation().getZ() - cr.cz;
        double dist = Math.sqrt(dx*dx + dz*dz);
        if (dist >= (cr.r - 200) && dist < cr.r) {
            e.getPlayer().spigot().sendMessage(
                ChatMessageType.ACTION_BAR,
                new TextComponent(ChatColor.YELLOW + "경계가 곧 나옵니다. (" + (int)(cr.r - dist) + "m 남음)")
            );
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("serveropskit.admin")) {
            sender.sendMessage(ChatColor.RED + "권한이 없습니다.");
            return true;
        }
        String name = command.getName();
        if (name.equalsIgnoreCase("opskit")) {
            if (args.length == 0) {
                sender.sendMessage(ChatColor.AQUA + "/opskit <status|reload|testwebhook|pause|resume|backup|restart>");
                return true;
            }
            String sub = args[0].toLowerCase(java.util.Locale.ROOT);
            switch (sub) {
                case "status":
                    sender.sendMessage(ChatColor.AQUA + "[ServerOpsKit] "
                            + "avgTick=" + String.format(java.util.Locale.ROOT,"%.1fms", rolling.avgTickMs)
                            + " estTPS=" + String.format(java.util.Locale.ROOT,"%.2f", rolling.estTps));
                    break;
                case "reload":
                    reloadConfig();
                    loadConfigValues();
                    sender.sendMessage(ChatColor.GREEN + "리로드 완료");
                    break;
                case "testwebhook":
                    sendDiscord("테스트", "웹훅 연결 테스트");
                    sender.sendMessage(ChatColor.GRAY + "웹훅 테스트 전송");
                    break;
                case "pause":
                    dispatch("chunky pause");
                    sender.sendMessage(ChatColor.YELLOW + "Chunky 일시정지");
                    break;
                case "resume":
                    if (pregenQuiet) dispatch("chunky quiet true");
                    dispatch("chunky continue");
                    sender.sendMessage(ChatColor.GREEN + "Chunky 재개");
                    break;
                case "backup":
                    try { runBackup(); sender.sendMessage(ChatColor.GREEN + "백업 완료"); }
                    catch (Exception e){ sender.sendMessage(ChatColor.RED + "백업 실패: " + e.getMessage()); }
                    break;
                case "restart":
                    dispatch(restartCommand);
                    sender.sendMessage(ChatColor.GOLD + "재시작 명령 실행");
                    break;
                default:
                    sender.sendMessage(ChatColor.AQUA + "/opskit <status|reload|testwebhook|pause|resume|backup|restart>");
            }
            return true;
        } else if (name.equalsIgnoreCase("청키")) {
            if (args.length == 0) { sender.sendMessage(ChatColor.AQUA + "사용법: /청키 <월드|중심|반경|모양|시작|일시정지|재개|정지|조용|상태> [값...]"); return true; }
            Map<String,String> m = new HashMap<>();
            m.put("월드","world"); m.put("중심","center"); m.put("반경","radius"); m.put("모양","shape");
            m.put("시작","start"); m.put("일시정지","pause"); m.put("정지","pause"); m.put("재개","continue"); m.put("조용","quiet true"); m.put("상태","info");
            String sub = m.getOrDefault(args[0], null);
            if (sub==null){ sender.sendMessage(ChatColor.RED + "알 수 없는 서브명령: " + args[0]); return true; }
            StringBuilder sb = new StringBuilder("chunky ").append(sub);
            for (int i=1;i<args.length;i++) sb.append(' ').append(args[i]);
            dispatch(sb.toString());
            sender.sendMessage(ChatColor.GRAY + "→ " + sb);
            return true;
        }
        return false;
    }

    private static class CenterRadius {
        final double cx, cz; final int r;
        CenterRadius(double cx, double cz, int r){ this.cx=cx; this.cz=cz; this.r=r; }
    }
}
