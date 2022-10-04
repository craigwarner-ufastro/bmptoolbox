package javaUFLib;

import javax.sound.sampled.*;
import java.io.*;

public class UFSounds {

    private Clip _asc;
    private boolean _playSound = true;
//-----------------------------------------------------------------------------------------

    public void Load(String filename) {
	try {
		_asc = AudioSystem.getClip();
		_asc.open( AudioSystem.getAudioInputStream(new File(filename)) );
		_asc.addLineListener(new LineListener(){
			public void update(LineEvent le) {
			    if (le.getType() == LineEvent.Type.STOP) _asc.close();
			}
		    });
	}
	catch (Exception e) {
	    System.err.println("UFSounds.Load> "+e.toString());
	}
    }

    public void play() {
	try {
	    if( _playSound ) _asc.start();
	}
	catch (Exception e) {
	    System.err.println("UFSounds.play> "+e.toString());
	}
    }
}

