package com.adrewdev.functional;

/**
 * Represents the absence of a value, similar to {@code void} but as a type.
 * 
 * <p>{@code Unit} is used when a method or operation needs to return a value
 * but there is no meaningful value to return. It's commonly used in functional
 * programming to represent side-effecting operations that can still fail.</p>
 * 
 * <p>Unlike {@code null}, {@code Unit} is a proper value that can be safely
 * used in functional chains without null checks.</p>
 * 
 * <p>Example:</p>
 * <pre>{@code
 * // Operation that returns Unit on success
 * Result<Unit, String> result = Result.success(Unit.VALUE)
 *     .tap(() -> System.out.println("Side effect"))
 *     .tap(() -> logger.log("Operation completed"));
 * 
 * // Check success without caring about the value
 * if (result.isSuccessful()) {
 *     System.out.println("Operation succeeded");
 * }
 * }</pre>
 * 
 * <p>This is equivalent to Kotlin's {@code Unit} type and Scala's {@code Unit}.</p>
 * 
 * @since 1.2.0
 * @author java-functional-extensions
 */
public final class Unit {
    
    /**
     * The single instance of Unit.
     */
    public static final Unit VALUE = new Unit();
    
    /**
     * Private constructor to prevent instantiation.
     */
    private Unit() {
        // Singleton pattern - only VALUE instance exists
    }
    
    /**
     * Returns the singleton Unit instance.
     * 
     * @return the Unit instance
     */
    public static Unit getInstance() {
        return VALUE;
    }
    
    @Override
    public boolean equals(Object obj) {
        return obj instanceof Unit;
    }
    
    @Override
    public int hashCode() {
        return 0;
    }
    
    @Override
    public String toString() {
        return "Unit";
    }
}
