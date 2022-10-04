package javaUFLib;

import java.util.HashMap;

public interface UFPidAgent {
	public void queryPidValues(String pidName);
	public HashMap<String, UFPidLoop> getPidMap();
}

