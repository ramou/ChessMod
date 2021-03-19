package chessmod;

import org.jetbrains.annotations.NotNull;

public class ModUtil {
	/**
	 * Returns null, while claiming to never return null.
	 * Useful for constants with @ObjectHolder who's values are null at compile time, but not at runtime
	 *
	 * @return null
	 */
	@SuppressWarnings("ConstantConditions")
	@NotNull
	// Get rid of "Returning null from Nonnull method" warnings
	public static <T> T _null() {
		return null;
	}
}
