package com.github.oct24th.batisty.proxy;

import com.github.oct24th.batisty.sql.SqlCommandKind;

public interface StatementIdSupplier {
    String createStatementId(SqlCommandKind sqlCommandKind);
}
