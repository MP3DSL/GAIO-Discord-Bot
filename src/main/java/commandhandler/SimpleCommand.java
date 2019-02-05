package commandhandler;

import java.lang.reflect.Method;

import commandhandler.Command.ExecutorType;

/**
 * 
 *Simplest command to make
 */
public class SimpleCommand{
    public final String name, description;
    private final ExecutorType executorType;
    private final Object object;
    private final Method method;
    private final int power;

    /**
     * Skeleton for a command
     * @param name Name of the Command
     * @param description Description of the command
     * @param executorType Where the command can be executed. ex:User, Console, All
     * @param object
     * @param method 
     * @param power Required Administrative Power to use the command
     */
    public SimpleCommand(String name, String description, ExecutorType executorType, Object object, Method method, int power){
        super();
        this.name = name;
        this.description = description;
        this.executorType = executorType;
        this.object = object;
        this.method = method;
        this.power = power;
    }

    public String getName(){
        return name;
    }

    public String getDescription(){
        return description;
    }

    public ExecutorType getExecutorType(){
        return executorType;
    }

    public Object getObject() {
		return object;
	}

	public Method getMethod() {
		return method;
	}
	
	public int getPower() {
		return power;
	}
}