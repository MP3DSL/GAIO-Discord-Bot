package commandhandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import gaiobot.Ref;
/**
 * Creates a custom @Command annotation for use when creating a user or console command.
 */
@Target(value=ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Command{
    public String name();
    public String description() default "No Description";
    public ExecutorType type() default ExecutorType.ALL;
    public int power() default Ref.defPower;

    public enum ExecutorType{
        ALL, USER, CONSOLE;
    }
}