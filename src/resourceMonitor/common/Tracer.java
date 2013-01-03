package resourceMonitor.common;

import org.apache.log4j.Logger;

public class Tracer 
{
    private Logger logger;
    
    private Tracer(Class<?> traceClass)
    {
        logger = Logger.getLogger(traceClass);
    }
    
    public static Tracer getTracer(Class<?> traceClass)
    {
        return new Tracer(traceClass);
    }
    
    public void trace(String format, Object... args)
    {
        if (this.logger.isTraceEnabled())
        {
            this.logger.trace(String.format(format, args));
        }
    }
    
    public void debug(String format, Object... args)
    {
        if (this.logger.isDebugEnabled())
        {
            this.logger.debug(String.format(format, args));
        }
    }
    
    public void info(String format, Object... args)
    {
        if (this.logger.isInfoEnabled())
        {
            this.logger.info(String.format(format, args));
        }
    }
    
    public void warn(String format, Object... args)
    {
        if (args.length != 0)
        {
            this.logger.warn(String.format(format, args));
        }
        else
        {
            this.logger.warn(format);
        }
    }
    
    public void error(String format, Object... args)
    {
        if (args.length != 0)
        {
            this.logger.error(String.format(format, args));
        }
        else
        {
            this.logger.error(format);
        }
    }
    
    public void fatal(String format, Object... args)
    {
        if (args.length != 0)
        {
            this.logger.fatal(String.format(format, args));
        }
        else
        {
            this.logger.fatal(format);
        }
    }
}
