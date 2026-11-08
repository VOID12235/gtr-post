package com.btr.platform.tenant;

import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Set;

@Component
public class BtrTenantLineHandler implements TenantLineHandler {

    private static final Set<String> IGNORED_TABLES = Set.of(
            "tenant",
            "sys_user",
            "sponsor",
            "campaign",
            "campaign_placement",
            "ad_event",
            "flyway_schema_history"
    );

    @Override
    public Expression getTenantId() {
        return new LongValue(TenantContext.require());
    }

    @Override
    public String getTenantIdColumn() {
        return "tenant_id";
    }

    @Override
    public boolean ignoreTable(String tableName) {
        return IGNORED_TABLES.contains(tableName.toLowerCase(Locale.ROOT));
    }
}
