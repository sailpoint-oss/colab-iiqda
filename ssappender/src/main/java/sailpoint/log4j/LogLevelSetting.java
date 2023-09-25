package sailpoint.log4j;

import java.io.Serializable;

import org.apache.log4j.Level;

public class LogLevelSetting implements Serializable {

	/**
	 * 
	 */
  private static final long serialVersionUID = 8280467894283195847L;

	public enum LogLevel {
		TRACE("Trace", Level.TRACE),
		DEBUG("Debug", Level.DEBUG),
		INFO("Info", Level.INFO),
		WARN("Warn", Level.WARN),
		ERROR("Error", Level.ERROR);

		private String label;
		private Level lvl;
		
		private LogLevel(String label, Level lvl) {
			this.label=label;
			this.lvl=lvl;
		}

		public String getLabel() {
			return label;
		}
		
		public Level getLevel() {
		  return lvl;
		}

		public static LogLevel fromString(String name) {
			for (LogLevel item : LogLevel.values()) {  
				if (item.getLabel().equals(name)) {  
					return item;  
				}  
			}  
			return LogLevel.ERROR;  
		}

		public static String[] labels() {
			LogLevel[] all=values();
			String[] lbl=new String[all.length];
			int idx=0;
			for(LogLevel lvl: all) {
				lbl[idx++]=lvl.getLabel();
			}
			return lbl;
		}

		public static LogLevel fromInt(Integer value) {
			LogLevel[] values=LogLevel.values();
			return values[value.intValue()];
		}

		public boolean wants(Level level) {
			int myLevel=this.ordinal();
			int msgLevel=LogLevel.valueOf(level.toString()).ordinal();
			if(myLevel<=msgLevel) return true;
			return false;
		}
	};

	private LogLevel level;
	private boolean enabled;
	private String logClass;

	public LogLevelSetting(boolean enabled, String logClass, LogLevel level) {
		this.enabled=enabled;
		this.logClass=logClass;
		this.level=level;
	}

	public LogLevel getLevel() {
		return level;
	}

	public void setLevel(LogLevel level) {
		this.level = level;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getLogClass() {
		return logClass;
	}

	public void setLogClass(String logClass) {
		this.logClass = logClass;
	}

	public Object getLevelInt() {
		return level.ordinal();
	}

}
