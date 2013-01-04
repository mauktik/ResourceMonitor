// ---------------------------------------------------------------------------
// Copyright 2012 Mauktik Gandhi
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ---------------------------------------------------------------------------
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
