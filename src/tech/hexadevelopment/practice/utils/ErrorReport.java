package tech.hexadevelopment.practice.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.message.Message;

import tech.hexadevelopment.practice.LegionPractice;

public class ErrorReport implements Filter {

	private String[] filters = new String[]{
			"attempted to establish connectio", "mysql", "query", "filemanager", "arenafile", "permission denied"
			};
	
	private long timestamp;
	private long counter;
	private LegionPractice plugin;

	public ErrorReport(LegionPractice plugin) {
		this.plugin = plugin;
	}


	private Result check(Level level, String message, Throwable t) {
		try{
			if(level == Level.WARN || level == Level.ERROR) {
				if(t != null){
					StringWriter errors = new StringWriter();
					t.printStackTrace(new PrintWriter(errors));
					String error = errors.toString();
					if(error.toLowerCase().contains(plugin.getName().toLowerCase())
							&& checkFilter(error)) {
						post("\n" + error);
					}
				}
				else if(message != null && message.toLowerCase().contains(plugin.getName().toLowerCase())
						&& checkFilter(message)) {
					post("\n" + message);
				}
			}
		}catch(Exception e) {}
		return Filter.Result.NEUTRAL;
	}
	
	private boolean checkFilter(String message) {
		String m = message.toLowerCase();
		for(String s : filters) {
			if(m.contains(s)) {
				return false;
			}
		}
		return true;
	}

	private void post(String message) {
		counter++;
		if(counter >= 20) {
			if(System.currentTimeMillis()-timestamp > 300*1000) {
				timestamp = System.currentTimeMillis();
				counter = 0;	
			}
			else return;
		}
		plugin.arenaPvP.post(message);
	}

	@Override
	public Result filter(LogEvent e) {
		return check(e.getLevel(), e.getMessage() != null ? e.getMessage().getFormattedMessage() : "", e.getThrown());
	}

	@Override
	public Result filter(Logger arg0, Level arg1, Marker arg2, String arg3, Object... arg4) {
		String s = "";
		for(Object o : arg4) {
			if(o != null) {
				s += o.toString();
			}
		}
		return check(arg1, arg3 + s, null);
	}

	@Override
	public Result filter(Logger arg0, Level arg1, Marker arg2, Object arg3, Throwable arg4) {
		return check(arg1, arg3 != null ? arg3.toString() : "null", arg4);
	}

	@Override
	public Result filter(Logger arg0, Level arg1, Marker arg2, Message arg3, Throwable arg4) {
		return check(arg1, arg3.getFormattedMessage(), arg4);
	}

	@Override
	public Result getOnMatch() {
		return Filter.Result.NEUTRAL;
	}

	@Override
	public Result getOnMismatch() {
		return Filter.Result.NEUTRAL;
	}




}
