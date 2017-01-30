/***************************************************************************
 * Copyright 2015 Kieker Project (http://kieker-monitoring.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ***************************************************************************/

package kieker.monitoring.writernew.filesystem;

import java.io.File;
import java.nio.file.Files;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import kieker.common.configuration.Configuration;
import kieker.common.record.misc.EmptyRecord;
import kieker.common.util.filesystem.FileExtensionFilter;
import kieker.monitoring.core.configuration.ConfigurationFactory;

/**
 * @author Christian Wulf
 *
 * @since 1.13
 */
public class BinaryFileWriterTest {

	@Rule
	public final TemporaryFolder tmpFolder = new TemporaryFolder(); // NOCS recommends that this is private. JUnit test wants this public.

	private Configuration configuration;

	@Before
	public void before() {
		this.configuration = new Configuration();
		this.configuration.setProperty(ConfigurationFactory.HOST_NAME, "testHostName");
		this.configuration.setProperty(ConfigurationFactory.CONTROLLER_NAME, "testControllerName");
		this.configuration.setProperty(BinaryFileWriter.CONFIG_BUFFERSIZE, "8192");
		this.configuration.setProperty(BinaryFileWriter.CONFIG_CHARSET_NAME, "UTF-8");
		this.configuration.setProperty(BinaryFileWriter.CONFIG_MAXLOGFILES, String.valueOf(Integer.MAX_VALUE));
		this.configuration.setProperty(BinaryFileWriter.CONFIG_MAXLOGSIZE, String.valueOf(Integer.MAX_VALUE));
		this.configuration.setProperty(BinaryFileWriter.CONFIG_PATH, this.tmpFolder.getRoot().getAbsolutePath());
	}

	@Test
	public void shouldCreateLogFolder() {
		// test preparation
		this.configuration.setProperty(BinaryFileWriter.CONFIG_MAXENTRIESINFILE, "1");
		this.configuration.setProperty(BinaryFileWriter.CONFIG_SHOULD_COMPRESS, "false");

		// test execution
		final BinaryFileWriter writer = new BinaryFileWriter(this.configuration);

		// test assertion
		Assert.assertTrue(Files.exists(writer.getLogFolder()));
	}

	@Test
	public void shouldCreateMappingAndRecordFiles() {
		// test preparation
		this.configuration.setProperty(BinaryFileWriter.CONFIG_MAXENTRIESINFILE, "1");
		this.configuration.setProperty(BinaryFileWriter.CONFIG_SHOULD_COMPRESS, "false");
		final BinaryFileWriter writer = new BinaryFileWriter(this.configuration);

		// test execution
		final File storePath = FilesystemTestUtil.executeFileWriterTest(1, writer);

		// test assertion
		final File[] mapFiles = storePath.listFiles(FileExtensionFilter.MAP);
		Assert.assertTrue(mapFiles[0].exists());
		Assert.assertThat(mapFiles.length, CoreMatchers.is(1));

		final File[] recordFiles = storePath.listFiles(FileExtensionFilter.BIN);
		Assert.assertTrue(recordFiles[0].exists());
		Assert.assertThat(recordFiles.length, CoreMatchers.is(1));
	}

	@Test
	public void shouldCreateMultipleRecordFiles() {
		// test preparation
		this.configuration.setProperty(BinaryFileWriter.CONFIG_MAXENTRIESINFILE, "2");
		this.configuration.setProperty(BinaryFileWriter.CONFIG_SHOULD_COMPRESS, "false");
		final BinaryFileWriter writer = new BinaryFileWriter(this.configuration);

		// test execution
		final File storePath = FilesystemTestUtil.executeFileWriterTest(3, writer);

		// test assertion
		final File[] mapFiles = storePath.listFiles(FileExtensionFilter.MAP);
		Assert.assertTrue(mapFiles[0].exists());
		Assert.assertThat(mapFiles.length, CoreMatchers.is(1));

		final File[] recordFiles = storePath.listFiles(FileExtensionFilter.BIN);
		Assert.assertTrue(recordFiles[0].exists());
		Assert.assertTrue(recordFiles[1].exists());
		Assert.assertThat(recordFiles.length, CoreMatchers.is(2));
	}

	@Test
	public void shouldCreateMultipleCompressedRecordFiles() {
		// test preparation
		this.configuration.setProperty(BinaryFileWriter.CONFIG_MAXENTRIESINFILE, "2");
		this.configuration.setProperty(BinaryFileWriter.CONFIG_SHOULD_COMPRESS, "true");
		final BinaryFileWriter writer = new BinaryFileWriter(this.configuration);

		// test execution
		final File storePath = FilesystemTestUtil.executeFileWriterTest(3, writer);

		// test assertion
		final File[] mapFiles = storePath.listFiles(FileExtensionFilter.MAP);
		Assert.assertTrue(mapFiles[0].exists());
		Assert.assertThat(mapFiles.length, CoreMatchers.is(1));

		final File[] recordFiles = storePath.listFiles(FileExtensionFilter.ZIP);
		Assert.assertTrue(recordFiles[0].exists());
		Assert.assertTrue(recordFiles[1].exists());
		Assert.assertThat(recordFiles.length, CoreMatchers.is(2));
	}

	@Test
	public final void testMaxLogFiles() {
		final int[] maxLogFilesValues = { -1, 0, 1, 2 };
		final int[] numRecordsToWriteValues = { 0, 1, 2, 3, 10 };
		final int[][] expectedNumRecordFilesValues = { { 0, 1, 1, 2, 5, }, { 0, 1, 1, 2, 5 }, { 0, 1, 1, 1, 1 }, { 0, 1, 1, 2, 2 } };

		for (int i = 0; i < maxLogFilesValues.length; i++) {
			final int maxLogFiles = maxLogFilesValues[i];

			for (int j = 0; j < numRecordsToWriteValues.length; j++) {
				final int numRecordsToWrite = numRecordsToWriteValues[j];

				// test preparation
				this.configuration.setProperty(BinaryFileWriter.CONFIG_MAXENTRIESINFILE, "2");
				this.configuration.setProperty(BinaryFileWriter.CONFIG_MAXLOGSIZE, "-1");
				this.configuration.setProperty(BinaryFileWriter.CONFIG_MAXLOGFILES, String.valueOf(maxLogFiles));
				final BinaryFileWriter writer = new BinaryFileWriter(this.configuration);

				// test execution
				final File storePath = FilesystemTestUtil.executeFileWriterTest(numRecordsToWrite, writer);

				// test assertion
				final String reasonMessage = "Passed arguments: maxLogFiles=" + maxLogFiles + ", numRecordsToWrite=" + numRecordsToWrite;
				final File[] recordFiles = storePath.listFiles(writer.getFileNameFilter());
				final int expectedNumRecordFiles = expectedNumRecordFilesValues[i][j];
				Assert.assertThat(reasonMessage, recordFiles.length, CoreMatchers.is(expectedNumRecordFiles));
			}
		}
	}

	@Test
	public void testMaxLogSize() throws Exception {
		final int recordSizeInBytes = 4 + 8 + EmptyRecord.SIZE;// 12

		// semantics of the tuple: (maxMegaBytesPerFile, megaBytesToWrite, expectedNumRecordFiles)
		final int[][] testInputTuples = {
			{ -1, 0, 0 }, { -1, 1, 1 },
			{ 0, 0, 0 }, { 0, 1, 1 },
			{ 1, 0, 0 }, { 1, 1, 1 }, { 1, 2, 2 }, { 1, 3, 2 },
		};

		for (final int[] testInputTuple : testInputTuples) {
			final int maxMegaBytesPerFile = testInputTuple[0];
			final int megaBytesToWrite = testInputTuple[1];
			final int expectedNumRecordFiles = testInputTuple[2];

			// test preparation
			final int numRecordsToWrite = (1024 * 1024 * megaBytesToWrite) / recordSizeInBytes;
			this.configuration.setProperty(BinaryFileWriter.CONFIG_MAXENTRIESINFILE, "-1");
			this.configuration.setProperty(BinaryFileWriter.CONFIG_MAXLOGSIZE, String.valueOf(maxMegaBytesPerFile));
			this.configuration.setProperty(BinaryFileWriter.CONFIG_MAXLOGFILES, "2");
			final BinaryFileWriter writer = new BinaryFileWriter(this.configuration);

			// test execution
			final File storePath = FilesystemTestUtil.executeFileWriterTest(numRecordsToWrite, writer);

			// test assertion
			final String reasonMessage = "Passed arguments: maxMegaBytesPerFile=" + maxMegaBytesPerFile + ", megaBytesToWrite=" + megaBytesToWrite;
			final File[] recordFiles = storePath.listFiles(writer.getFileNameFilter());
			Assert.assertThat(reasonMessage, recordFiles.length, CoreMatchers.is(expectedNumRecordFiles));
		}
	}

}
