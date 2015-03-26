package kr.co.shineware.nlp.komoran.analysis.support;

public final class MeCabLoader {
	private volatile static MeCabLoader uniqueInstance;
	private static Model model;
	static {
		try {
			System.loadLibrary("MeCab");
		} catch (UnsatisfiedLinkError e) {
			System.err
					.println("Cannot load the native code.\n"
							+ "Make sure your LD_LIBRARY_PATH contains MeCab.so path.\n"
							+ e);
			System.exit(1);
		}
	}

	public static MeCabLoader getInstance(String dicDir)
			throws NullPointerException, RuntimeException {
		// DCL(Double-checking Locking) using Volatile Singleton. thread-safe
		// http://en.wikipedia.org/wiki/Double-checked_locking#Usage_in_Java 참조
		MeCabLoader result = uniqueInstance;
		if (result == null) {
			synchronized (MeCabLoader.class) {
				result = uniqueInstance;
				if (result == null) {
					uniqueInstance = result = new MeCabLoader(dicDir);
				}
			}
		}
		return result;
	}

	private MeCabLoader(String dicDir) {
		model = new Model("-d " + dicDir);
	}

	public Tagger createTagger() {
		return model.createTagger();
	}

	public Lattice createLattice() {
		return model.createLattice();
	}
}