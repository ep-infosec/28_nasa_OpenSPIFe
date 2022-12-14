/*******************************************************************************
 * Copyright 2014 United States Government as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package gov.nasa.ensemble.core.model.plan;

import gov.nasa.ensemble.common.logging.LogUtil;
import gov.nasa.ensemble.common.mission.MissionExtendable;
import gov.nasa.ensemble.common.mission.MissionExtender;
import gov.nasa.ensemble.common.mission.MissionExtender.ConstructionException;

import java.lang.management.ManagementFactory;
import java.util.UUID;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.util.EcoreUtil;

/**
 * A concrete base class for generating unique ID strings for diffs.
 * The client fetches the singleton instance and calls a method to
 * fetch the ID string.
 */
public class DiffIdGenerator implements MissionExtendable {

	/** The singleton instance, lazily created. */
	protected static DiffIdGenerator INSTANCE = null;
	
	private static int nextSequentialSuffix = 1;
	
	protected final static long SESSION_ID_PREFIX = generateUniqueIdNumber();
	
	/**
	 * Return the singleton instance. When called from a subclass, it
	 * will return an instance of that type.
	 * @return the singleton instance
	 */
	public static DiffIdGenerator getInstance() {
		if (INSTANCE == null) {
			try {
				INSTANCE = MissionExtender.construct(DiffIdGenerator.class);
			} catch (ConstructionException e) {
				LogUtil.error("constructing DiffIdGenerator");
				INSTANCE = new DiffIdGenerator();
			}
		}
		return INSTANCE;
	}
	
	/**
	 * Return a unique ID string. This overridden method is called if the
	 * EClass argument is not null but also not one of the recognized ones
	 * (activity, activity group, plan or profile).
	 * @param eClass the class of the object for which a diff ID is requested
	 * @return a unique ID string
	 */
	public String generateDiffId(EClass eClass) {
		return EcoreUtil.generateUUID();
	}
	
	/**
	 * Return a unique ID string. This overridden method is called if the
	 * EClass argument is not null but also not one of the recognized ones
	 * (activity, activity group, plan or profile).
	 * @param eClass the class of the object for which a diff ID is requested
	 * @param originator is used by the Score implementation to set part of the id; by default it's ignored.
	 * @return a unique ID string
	 */
	public String generateDiffId(EClass eClass, String originator) {
		return generateDiffId(eClass);
	}

	public static String createUUID() {
		return getInstance().instanceUUID();
	}
	
	protected String instanceUUID() {
		return UUID.randomUUID().toString();
	}
	
	
	/** Generate a completely unique and unguessable id each time. */
	public static long generateUniqueIdNumber() {
		UUID randomUUID = UUID.randomUUID();
		long source1a = randomUUID.getMostSignificantBits();
		long source1b = randomUUID.getLeastSignificantBits();
		UUID jvmUUID = UUID.nameUUIDFromBytes(ManagementFactory.getOperatingSystemMXBean().getName().getBytes());
		long source2a = jvmUUID.getMostSignificantBits();
		long source2b = jvmUUID.getLeastSignificantBits();
		long source3 = System.nanoTime();
		long xor = source1a ^ source1b ^ source2a ^ source2b ^ source3;
		return xor < 0? ~xor : xor;
	}
	
	/** 
	 * Generate an id with a prefix that is unique but stays the same throughout the run,
	 * with a sequentially increasing counter at the end which is guaranteed never to repeat
	 * during the run because it's synchronized.  If it gets large enough to spill over the
	 * allocated length, it doesn't wrap around, but starts eating into (carried the 1 into)
	 * the end of the prefix.
	 * <p>
	 * Rationale:  <ol>
	 * <li> Unlike with cookie applications, a plan file is not trying to
	 * make the next id unguessable or hide its source.  On the contrary, it may be useful
	 * for troubleshooting to see what was generated by one run of, say, Score and in what order.
	 * <li> If the need arises to manually compare id's, it focuses attention on the part
	 * that is known to change.
	 * @param desiredLength -- number of characters to return (limited by the number of bits in a long over the log base 2 of the radix)
	 * @param nDigitsReservedForCounter -- not exactly "reserved"; can split over; e.g. calling generateSequentiallyIncreasingIdNumber(6, 3, 10) a might yield "742001", "742002", ..., "742998", "742999", "743000" 
	 * @param radix -- 10 for decimal, 16 for hexadecimal, 36 for base 36, etc.
	 * @return
	 */
	public String generateSequentiallyIncreasingIdNumber(int desiredLength, int nDigitsReservedForCounter, int radix) {
		return generateSequentiallyIncreasingIdNumber(SESSION_ID_PREFIX, desiredLength, nDigitsReservedForCounter, radix);
	}

	/**
	 * @see generateSequentiallyIncreasingIdNumber(int, int, int)}
	 */
	public String generateSequentiallyIncreasingIdNumber(long sessionIdPrefix, int desiredLength, int nDigitsReservedForCounter, int radix) {
		final int MAX_BITS = Long.SIZE-1;
		desiredLength = (int) Math.min(desiredLength, Math.floor(MAX_BITS / (Math.log(radix)/Math.log(2))));
		int desiredPrefixLength = desiredLength - nDigitsReservedForCounter;
		long multiplier = (long)Math.pow(radix, nDigitsReservedForCounter);
		long prefix = sessionIdPrefix % (long) Math.pow(radix, desiredPrefixLength);
		if (prefix < Math.pow(radix, desiredPrefixLength-1)) {
			prefix += Math.pow(radix, desiredPrefixLength-1);
		}
		long counter = generateNextSequentialCounter();
		long id = prefix * multiplier + counter;
		if (id < 0) {
			LogUtil.error("Generated a negative id -- should be impossible.");
		}
		String proposedResult = Long.toString(id, radix);
		if (proposedResult.length() != desiredLength) {
			LogUtil.warn("Id generation bug:  Returning id of " + id + " which is of length " + proposedResult.length() + ", not " + desiredLength);
		}
		return proposedResult;
	}

	public synchronized long generateNextSequentialCounter() {
		return nextSequentialSuffix++;
	}	

	/**
	 * Return a unique ID string.
	 * @param original gives the eClass and a hint about preserving info, used by Score implementation to preserve originator.
	 * @return a unique ID string
	 */
	public String generateDiffIdForCopy(EPlanElement original) {
		return generateDiffId(original.eClass());
	}
	
	public static long xorHashCodes(long maxPlusOne, Object... dataDeterminingId) {
		long xor = 0;
		for (Object object : dataDeterminingId) {
			if (object != null) {
				xor ^= object.hashCode();
			}
		}
		return (xor < 0? ~xor : xor) % maxPlusOne;
	}
}
