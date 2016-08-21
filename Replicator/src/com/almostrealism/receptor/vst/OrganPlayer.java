/*
 * Copyright 2016 Michael Murray
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.almostrealism.receptor.vst;

import com.almostrealism.feedgrow.audio.AudioProteinCache;
import com.almostrealism.feedgrow.cellular.CellAdapter;
import com.almostrealism.feedgrow.metering.AudioMeter;
import com.almostrealism.feedgrow.organ.Organ;
import com.almostrealism.feedgrow.organ.SimpleOrgan;

import jvst.wrapper.VSTPluginAdapter;

public class OrganPlayer extends VSTPluginAdapter {
	private AudioMeter audio;
	
	private PopulationProgramSet programs;
	private Organ<Long> currentProgram;
	
	private float buf[];
	
	public OrganPlayer(long wrapper) {
		super(wrapper);
		
		this.audio = new AudioMeter(new AudioProteinCache());
		this.audio.setTextOutputEnabled(false);
		
		// communicate with the host
		this.setNumInputs(1); // mono input
		this.setNumOutputs(1); // mono output
		// this.hasVu(false); //deprecated as of vst2.4
		this.canProcessReplacing(true); // mandatory for vst 2.4!
		this.setUniqueID(9876543);// random unique number registered at
									// steinberg (4 byte)
		
		this.canMono(true);
		
		this.programs = new PopulationProgramSet(System.getProperty("user.home") + "/FeedGrow/Population.xml");
		setProgram(0);
	}
	
	/**
	 * Notifies the {@link AudioProteinCache} of the new sample rate.
	 */
	public void setSampleRate(float sampleRate) { AudioProteinCache.sampleRate = (int) sampleRate; }
	
	public int canDo(String feature) {
		// the host asks us here what we are able to do
		int ret = CANDO_NO;
		if (feature.equals(CANDO_PLUG_1_IN_1_OUT))
			ret = CANDO_YES;
		if (feature.equals(CANDO_PLUG_PLUG_AS_CHANNEL_INSERT))
			ret = CANDO_YES;
		if (feature.equals(CANDO_PLUG_PLUG_AS_SEND))
			ret = CANDO_YES;
		
		log("canDo: " + feature + " = " + ret);
		return ret;
	}

	public String getProductString() { return "FeedGrow"; }
	public String getEffectName() { return "FeedGrow"; }
	public String getVendorString() { return "http://almostrealism.com"; }
	public String getProgramNameIndexed(int category, int index) { return "program: cat: " + category + ", " + index; }
	public boolean setBypass(boolean value) { return false; }
	
	public boolean string2Parameter(int index, String value) {
		try {
			if (value != null)
				this.setParameter(index, Float.parseFloat(value));
			return true;
		} catch (Exception e) { // ignore
			return false;
		}
	}
	
	public int getNumParams() { return programs.getParameterCount(currentProgram); }
	public int getNumPrograms() { return programs.size(); }
	public float getParameter(int index) { return programs.getParameter(currentProgram, index); }
	public String getParameterLabel(int index) { return programs.getParameterLabel(currentProgram, index); }
	public String getParameterName(int index) { return programs.getParameterName(currentProgram, index); }
	
	public String getParameterDisplay(int index) {
		return String.valueOf(((int) (100 * programs.getParameterMultiplier(currentProgram, index) * getParameter(index))) / 100.0);
	}
	
	public int getProgram() { return programs.indexOf(currentProgram); }
	public String getProgramName() { return "program " + programs.getProgramName(currentProgram); }
	public void setParameter(int index, float value) { programs.setParameter(currentProgram, index, value); }
	
	public void setProgram(int index) {
		currentProgram = programs.getOrgan(index);
		currentProgram.setProteinCache(audio.getProteinCache());
		((CellAdapter<Long>) ((SimpleOrgan<Long>) currentProgram).firstCell()).setMeter(audio);
	}
	
	public void setProgramName(String name) { programs.setProgramName(currentProgram, name); }
	public int getPlugCategory() { return PLUG_CATEG_EFFECT; }
	
	// Generate / Process the sound!
	public void processReplacing(float[][] inputs, float[][] outputs, int sampleFrames) {
		float[] inBuffer = inputs[0];
		float[] outBuffer = outputs[0];
		
		for (int i = 0, n = sampleFrames; i < n; i++) {
			float exVal = inBuffer[i];

			float out = buf[i];
			outBuffer[i] = out;
			
		}
	}
}
