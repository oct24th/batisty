package io.github.oct24th.batisty.proxy;

import io.github.oct24th.batisty.enums.SqlCommandKind;

public interface StatementIdSupplier {
    String createStatementId(SqlCommandKind sqlCommandKind);
}
