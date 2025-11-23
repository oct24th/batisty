package io.github.oct24th.batisty.enums;

import lombok.Getter;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.StatementType;

@Getter
public enum SqlCommandKind {
    COUNT(SqlCommandType.SELECT, StatementType.PREPARED, 1),
    SELECT(SqlCommandType.SELECT, StatementType.PREPARED, 1),
    INSERT(SqlCommandType.INSERT, StatementType.PREPARED, 1),
    UPDATE(SqlCommandType.UPDATE, StatementType.PREPARED, 2),
    DELETE(SqlCommandType.DELETE, StatementType.PREPARED, 1),
    PROCEDURE(SqlCommandType.UNKNOWN, StatementType.CALLABLE, 0),
    FUNCTION(SqlCommandType.SELECT, StatementType.PREPARED, 0);

    private final SqlCommandType sqlCommandType;
    private final StatementType statementType;
    private final int dataStoreCount;

    SqlCommandKind(SqlCommandType sqlCommandType, StatementType statementType, int dataStoreCount){
        this.sqlCommandType = sqlCommandType;
        this.statementType = statementType;
        this.dataStoreCount = dataStoreCount;
    }
}
