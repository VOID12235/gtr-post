package com.btr.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.btr.platform.entity.SysConfig;
import com.btr.platform.mapper.SysConfigMapper;
import com.btr.platform.tenant.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Service
public class ConfigService {

    private static final Set<String> GROUPS = Set.of("site", "mail", "upload", "payment", "security");

    private static final Map<String, Map<String, String>> DEFAULTS = Map.of(
            "site", ordered(
                    "name", "BTR 社会赛事平台",
                    "footer", "主办方免费 · 赞助商付费 · 证书可验真",
                    "logo", "/brand/logo-horizontal.svg",
                    "about", "面向企业、行业协会、政府/园区、品牌的社会赛事基础设施。"
            ),
            "mail", ordered(
                    "enabled", "false",
                    "host", "",
                    "port", "465",
                    "username", "",
                    "password", "",
                    "from", "BTR 赛事平台",
                    "ssl", "true"
            ),
            "upload", ordered(
                    "max_size_mb", "50",
                    "allowed_types", "jpg,jpeg,png,gif,webp,svg,pdf,zip,rar,7z,doc,docx,ppt,pptx,xls,xlsx,txt,md,mp4,mov"
            ),
            "payment", ordered(
                    "manual_enabled", "true",
                    "instructions", "报名后请联系主办方完成线下支付，管理员确认后报名生效。"
            ),
            "security", ordered(
                    "login_fail_limit", "5",
                    "login_lock_minutes", "10"
            )
    );

    private final SysConfigMapper configMapper;
    private final SecretCodec secretCodec;

    public ConfigService(SysConfigMapper configMapper, SecretCodec secretCodec) {
        this.configMapper = configMapper;
        this.secretCodec = secretCodec;
    }

    public Map<String, String> getGroup(String group) {
        validateGroup(group);
        Map<String, String> result = new LinkedHashMap<>(DEFAULTS.get(group));
        for (SysConfig config : configMapper.selectList(new QueryWrapper<SysConfig>()
                .eq("config_group", group))) {
            String value = config.configValue;
            if ("password".equals(keyName(config.configKey))) {
                value = "******";
            }
            result.put(keyName(config.configKey), value);
        }
        return result;
    }

    public Map<String, String> getRawGroup(String group) {
        validateGroup(group);
        Map<String, String> result = new LinkedHashMap<>(DEFAULTS.get(group));
        for (SysConfig config : configMapper.selectList(new QueryWrapper<SysConfig>()
                .eq("config_group", group))) {
            String value = config.configValue;
            if ("password".equals(keyName(config.configKey))) {
                value = secretCodec.decrypt(value);
            }
            result.put(keyName(config.configKey), value);
        }
        return result;
    }

    public String get(String group, String key, String fallback) {
        return getRawGroup(group).getOrDefault(key, fallback);
    }

    public Map<String, String> getPublicSite() {
        Map<String, String> site = getRawGroup("site");
        return Map.of(
                "name", site.getOrDefault("name", "BTR 社会赛事平台"),
                "footer", site.getOrDefault("footer", ""),
                "logo", site.getOrDefault("logo", "/brand/logo-horizontal.svg")
        );
    }

    @Transactional
    public Map<String, String> updateGroup(String group, Map<String, String> values) {
        validateGroup(group);
        Long tenantId = TenantContext.require();
        for (Map.Entry<String, String> entry : values.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (key == null || key.isBlank() || key.contains(".")) {
                continue;
            }
            String fullKey = group + "." + key;
            if ("password".equals(key) && (value == null || value.isBlank() || "******".equals(value))) {
                continue;
            }
            if ("password".equals(key) && value != null) {
                value = secretCodec.encrypt(value);
            }
            SysConfig existing = configMapper.selectOne(new QueryWrapper<SysConfig>()
                    .eq("tenant_id", tenantId)
                    .eq("config_key", fullKey));
            if (existing == null) {
                SysConfig config = new SysConfig();
                config.tenantId = tenantId;
                config.configGroup = group;
                config.configKey = fullKey;
                config.configValue = value;
                config.updatedAt = LocalDateTime.now();
                configMapper.insert(config);
            } else {
                existing.configValue = value;
                existing.updatedAt = LocalDateTime.now();
                configMapper.updateById(existing);
            }
        }
        return getGroup(group);
    }

    private String keyName(String fullKey) {
        int index = fullKey.indexOf('.');
        return index >= 0 ? fullKey.substring(index + 1) : fullKey;
    }

    private void validateGroup(String group) {
        if (!GROUPS.contains(group)) {
            throw new IllegalArgumentException("Unknown config group: " + group);
        }
    }

    private static String escape(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static Map<String, String> ordered(String... pairs) {        Map<String, String> map = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            map.put(pairs[i], pairs[i + 1]);
        }
        return map;
    }
}
