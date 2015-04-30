package ser;

import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;

import java.io.*;

/**
 * Created by ruedi on 30/04/15.
 */
public class Git64 {

    // workarounded solution to fst eager input buffer reading issue
	public static void main(String[] args) throws Exception {

		File temp = File.createTempFile("test", "dat");

		final int BUFFER_SIZE_IN_BYTES = 10 * 1024 * 1024;
		final int MAX_ITEMS_BEFORE_FLUSH = 10000;
		final int NUMBER_OF_ITEMS = 1000000;

		try {

			FSTConfiguration config = FSTConfiguration.getDefaultConfiguration();
			int numberOfObjects = 0;

			try (FileOutputStream fileOutputStream = new FileOutputStream(temp)) {

				try (BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream, BUFFER_SIZE_IN_BYTES)) {
                    for (int i = 0; i < NUMBER_OF_ITEMS; i++) {

                        Object[] arr = new Object[100];
                        for (int objIdx = 0; objIdx < arr.length; objIdx++) {
                            arr[objIdx] = "row " + i + " - " + "my object" + objIdx;
                        }

                        config.encodeToStream(bufferedOutputStream,arr);
                        numberOfObjects++;

                        if (i % MAX_ITEMS_BEFORE_FLUSH == 0) {
                            System.out.println("writing " + i);
                        }
                    }
				}
			}

			System.out.println("done with write");

			try (FileInputStream fileInputStream = new FileInputStream(temp)) {
				try (BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream, BUFFER_SIZE_IN_BYTES)) {
                    for (int idx = 0; idx < numberOfObjects; idx++) {
                        Object[] row = (Object[]) config.decodeFromStream(bufferedInputStream);
                        if (idx % MAX_ITEMS_BEFORE_FLUSH == 0) {
                            System.out.println("reading " + idx);
                        }
                    }
				}
			}

			System.out.println("done with read");
		}
		finally {
			temp.delete();
		}

	}


    // this one fails
	public static void main0(String[] args) throws Exception {

		File temp = File.createTempFile("test", "dat");

		final int BUFFER_SIZE_IN_BYTES = 10 * 1024 * 1024;
		final int MAX_ITEMS_BEFORE_FLUSH = 10000;
		final int NUMBER_OF_ITEMS = 1000000;

		try {

			FSTConfiguration config = FSTConfiguration.getDefaultConfiguration();
			config.setPreferSpeed(true);
			config.setShareReferences(false);

			int numberOfObjects = 0;

			try (FileOutputStream fileOutputStream = new FileOutputStream(temp)) {

				try (BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream, BUFFER_SIZE_IN_BYTES)) {

					try (FSTObjectOutput fstObjectOutput = new FSTObjectOutput(bufferedOutputStream, config)) {

						for (int i = 0; i < NUMBER_OF_ITEMS; i++) {
							Object[] arr = new Object[100];

							for (int objIdx = 0; objIdx < arr.length; objIdx++) {
								arr[objIdx] = "row " + i + " - " + "my object" + objIdx;
							}

							fstObjectOutput.writeObject(arr);

							numberOfObjects++;

							if (i % MAX_ITEMS_BEFORE_FLUSH == 0) {

								System.out.println("writing " + i);
								fstObjectOutput.flush();
							}
						}
					}
				}
			}

			System.out.println("done with write");

			try (FileInputStream fileInputStream = new FileInputStream(temp)) {

				try (BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream, BUFFER_SIZE_IN_BYTES)) {

					try (FSTObjectInput fstObjectInput = new FSTObjectInput(bufferedInputStream, config)) {

						for (int idx = 0; idx < numberOfObjects; idx++) {

							Object[] row = (Object[]) fstObjectInput.readObject();

							if (idx % MAX_ITEMS_BEFORE_FLUSH == 0) {

								System.out.println("reading " + idx);

							}
						}
					}
				}
			}

			System.out.println("done with read");
		}
		finally {
			temp.delete();
		}

	}
}
