package net.sf.j3d.network.db;

/**
 * The OutputHandler interface is implemented by classes that wish to be notified when job output
 * is sent to a DatabaseConnection object to be stored.
 */
public interface OutputHandler {
	public void storeOutput(long time, int uid, JobOutput output);
}