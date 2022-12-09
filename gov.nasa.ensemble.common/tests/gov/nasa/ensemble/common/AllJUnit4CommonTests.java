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
package gov.nasa.ensemble.common;

import gov.nasa.ensemble.common.functional.TestFunctions;
import gov.nasa.ensemble.common.functional.TestLists;
import gov.nasa.ensemble.common.functional.TestParser;
import gov.nasa.ensemble.common.functional.TestPromises;
import gov.nasa.ensemble.common.functional.TestRead;
import gov.nasa.ensemble.common.metadata.TestMetadata;
import gov.nasa.ensemble.common.reflection.TestReflectionUtils;
import gov.nasa.ensemble.common.system.TestCommandExecutor;
import gov.nasa.ensemble.common.thread.TestThreadUtils;
import gov.nasa.ensemble.common.time.TestTimeConversions;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	TestCommonUtils.class,
	TestMetadata.class,
	TestThreadUtils.class,
	TestReflectionUtils.class,
	TestCommandExecutor.class,
	
	// gov.nasa.ensemble.common.functional
	TestFunctions.class,
	TestLists.class,
	TestParser.class,
	TestRead.class,
	TestPromises.class,
	
	// gov.nasa.ensemble.common.time
	TestTimeConversions.class,
})
public class AllJUnit4CommonTests extends EnsembleJUnit4Suite { /**/ }
