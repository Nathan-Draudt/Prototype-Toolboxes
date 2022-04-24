/*
*      _______                       _____   _____ _____  
*     |__   __|                     |  __ \ / ____|  __ \ 
*        | | __ _ _ __ ___  ___  ___| |  | | (___ | |__) |
*        | |/ _` | '__/ __|/ _ \/ __| |  | |\___ \|  ___/ 
*        | | (_| | |  \__ \ (_) \__ \ |__| |____) | |     
*        |_|\__,_|_|  |___/\___/|___/_____/|_____/|_|     
*                                                         
* -------------------------------------------------------------
*
* TarsosDSP is developed by Joren Six at IPEM, University Ghent
*  
* -------------------------------------------------------------
*
*  Info: http://0110.be/tag/TarsosDSP
*  Github: https://github.com/JorenSix/TarsosDSP
*  Releases: http://0110.be/releases/TarsosDSP/
*  
*  TarsosDSP includes modified source code by various authors,
*  for credits and info, see README.
* 
*/

package lib.TarsosDSP.pitch;

import org.apache.commons.lang3.ArrayUtils;

/**
 * <p>
 * A pitch extractor that extracts the Average Magnitude Difference (AMDF) from
 * an audio buffer. This is a good measure of the Pitch (f0) of a signal.
 * </p>
 * <p>
 * AMDF is calculated by the the difference between the waveform summing a
 * lagged version of itself.
 * </p>
 * <p>
 * The main bulk of the code is written by Eder de Souza for the <a
 * href="http://jaudio.sf.net">jAudio</a> framework. Adapted for TarsosDSP by
 * Joren Six.
 * </p>
 * 
 * @author Eder Souza (ederwander on github)
 * @author Joren Six
 */
public class AMDF implements PitchDetector{
	
	
	public static final double DEFAULT_MIN_FREQUENCY = 82.0;
	public static final double DEFAULT_MAX_FREQUENCY = 1000.0;
	public static final double DEFAULT_RATIO = 5.0;
	public static final double DEFAULT_SENSITIVITY = 0.1;
	
	public static float sampleRate;
	
	public static double[] amd;
	public static long maxPeriod;
	public static long minPeriod;	
	public static double ratio;
	public static double sensitivity;
	
	/**
	 * The result of the pitch detection iteration.
	 */
	public static PitchDetectionResult result;

	public AMDF(float sampleRate, int bufferSize) {
		this(sampleRate,bufferSize,DEFAULT_MIN_FREQUENCY,DEFAULT_MAX_FREQUENCY);
	}
	
	
	public AMDF(float sampleRate, int bufferSize,double minFrequency,double maxFrequency) {
		this.sampleRate = sampleRate;
		amd = new double[bufferSize];
		this.ratio = DEFAULT_RATIO;
		this.sensitivity = DEFAULT_SENSITIVITY;
		this.maxPeriod = Math.round(sampleRate / minFrequency + 0.5);
		this.minPeriod = Math.round(sampleRate / maxFrequency + 0.5);
		result = new PitchDetectionResult();
	}

	/* public AMDF(float sampleRate, double[] amd,long maxPeriod, long minPeriod, double ratio, double sensitivity, int bufferSize,double minFrequency,double maxFrequency) {
		AMDF.amd= amd;	
		this.ratio=ratio;
		this.sensitivity=sensitivity;
		this.sampleRate = sampleRate;
		this.maxPeriod = Math.round(sampleRate / minFrequency + 0.5);
		this.minPeriod = Math.round(sampleRate / maxFrequency + 0.5);
		result = new PitchDetectionResult();
	} */

	//@Override
	public static PitchDetectionResult AvgPitch(float[] audioBuffer) {
		//System.out.println(ArrayUtils.toString(audioBuffer));
		
		int t = 0;
		float f0 = -1; //so nothing is happening to it...why
		double minval = Double.POSITIVE_INFINITY;
		double maxval = Double.NEGATIVE_INFINITY;
		double[] frames1 = new double[0];
		double[] frames2 = new double[0];
		double[] calcSub = new double[0];

		System.out.print("Lenght: "+audioBuffer.length);

		int maxShift = audioBuffer.length;
		//System.out.print(maxShift);
		//System.out.print("                    ");
		/* for(int i=0; i < amd.length; i++){
			System.out.print("  "+amd[i]+"   ");
		} */
		
		amd = new double[audioBuffer.length];

		PitchDetectionResult result = new PitchDetectionResult();

		for (int i = 0; i < maxShift; i++) {
			frames1 = new double[maxShift - i + 1];
			frames2 = new double[maxShift - i + 1];
			t = 0;
			for (int aux1 = 0; aux1 < maxShift - i; aux1++) {
				t = t + 1; //counter from frames
				frames1[t] = audioBuffer[aux1]; //Frames1

			}
			//System.out.println(ArrayUtils.toString(frames1));
			t = 0;
			for (int aux2 = i; aux2 < maxShift; aux2++) {
				t = t + 1;
				frames2[t] = audioBuffer[aux2];
			}
			//System.out.println(ArrayUtils.toString(frames2));

			int frameLength = frames1.length;
			calcSub = new double[frameLength];
			for (int u = 0; u < frameLength; u++) {
				calcSub[u] = frames1[u] - frames2[u];
			}

			double summation = 0;
			for (int l = 0; l < frameLength; l++) {
				summation +=  Math.abs(calcSub[l]);
			}
			amd[i] = summation; //lots of different summations of differences between 2 arrays (that )
			//System.out.print(amd[i]+"     ");
		}

		for (int j = (int)minPeriod; j < (int)maxPeriod; j++){
			if(amd[j] < minval){
				 minval = amd[j];
			}
			if(amd[j] > maxval)	{
				 maxval = amd[j];
			}
		}
		int cutoff = (int) Math.round((sensitivity * (maxval - minval)) + minval);
		int j=(int)minPeriod;
		
		while(j<=(int)maxPeriod && (amd[j] > cutoff)){
			j=j+1;
		}
		
		double search_length = minPeriod / 2;
		minval = amd[j];
		int minpos = j;
		int i=j;
		while((i<j+search_length) && (i<=maxPeriod)){
			i=i+1;
			if(amd[i] < minval){
		          minval = amd[i];
		          minpos = i;
			}
		}

		if(Math.round(amd[minpos] * ratio) < maxval){
			f0 = sampleRate/minpos;
		}
		
		result.setPitch(f0);
		result.setPitched(-1!=f0);
		result.setProbability(-1);

		return result;
	}	
}
