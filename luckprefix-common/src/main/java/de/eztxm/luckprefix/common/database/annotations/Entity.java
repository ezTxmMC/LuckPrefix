package de.eztxm.luckprefix.common.database.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElemenTtType.TYPE)
public @interface Entity {

    String table() default "";
    String collection() default "";
    String database() default "";
    DatabaseType[] supportedDatabases() default { DatabaseType.MONGODB, DatabaseType.SQLITE, DatabaseType.MARIADB }

}
