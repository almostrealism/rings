package com.wifi_cell.voip;

import java.io.IOException;
import javax.microedition.media.Control;
import javax.microedition.media.protocol.DataSource;
import javax.microedition.media.protocol.SourceStream;

public class StreamingDataSource extends DataSource {

  public StreamingDataSource(String locator) {
		super(locator);
	}
  
  private String locator;
  private SourceStream[] streams;
  private boolean connected = false;

  public void setLocator(String locator) { this.locator = locator; }

  public String getLocator() { return locator; }

  public void connect() throws IOException {
	}

  public void disconnect() {
	}

  public void start() throws IOException {
	}

  public void stop() throws IOException {

	}

  public String getContentType() {
		// for the purposes of this article, it is only video/mpeg
		return "audio/rtp";
	}

  public Control[] getControls() { return new Control[0]; }

  public Control getControl(String controlType) { return null; }

  public SourceStream[] getStreams() {	return streams; }

}