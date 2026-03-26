package com.adrewdev.functional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for the Maybe class.
 * Tests cover factory methods, predicates, value extraction, and basic operations.
 */
@DisplayName("Maybe")
class MaybeTest {

    @Nested
    @DisplayName("from()")
    class FromMethod {

        @Test
        @DisplayName("with non-null value returns Some")
        void from_withValue_returnsSome() {
            Maybe<String> maybe = Maybe.from("test");

            assertThat(maybe.isSome()).isTrue();
            assertThat(maybe.isNone()).isFalse();
            assertThat(maybe.getValueOrThrow()).isEqualTo("test");
        }

        @Test
        @DisplayName("with null value returns None")
        void from_withNull_returnsNone() {
            Maybe<String> maybe = Maybe.from(null);

            assertThat(maybe.isNone()).isTrue();
            assertThat(maybe.isSome()).isFalse();
        }
    }

    @Nested
    @DisplayName("some()")
    class SomeMethod {

        @Test
        @DisplayName("with non-null value returns Some")
        void some_withValue_returnsSome() {
            Maybe<String> maybe = Maybe.some("test");

            assertThat(maybe.isSome()).isTrue();
            assertThat(maybe.getValueOrThrow()).isEqualTo("test");
        }

        @Test
        @DisplayName("with null value throws NullPointerException")
        void some_withNull_throwsNPE() {
            assertThatThrownBy(() -> Maybe.some(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("value cannot be null");
        }
    }

    @Nested
    @DisplayName("none()")
    class NoneMethod {

        @Test
        @DisplayName("returns None")
        void none_returnsNone() {
            Maybe<String> maybe = Maybe.none();

            assertThat(maybe.isNone()).isTrue();
            assertThat(maybe.isSome()).isFalse();
        }

        @Test
        @DisplayName("is singleton - all calls return same instance")
        void none_isSingleton() {
            Maybe<String> none1 = Maybe.none();
            Maybe<String> none2 = Maybe.none();
            Maybe<Integer> none3 = Maybe.none();

            assertThat(none1).isSameAs(none2);
            assertThat(none1).isSameAs(none3);
        }
    }

    @Nested
    @DisplayName("empty()")
    class EmptyMethod {

        @Test
        @DisplayName("returns None")
        void empty_returnsNone() {
            Maybe<String> maybe = Maybe.empty();

            assertThat(maybe.isNone()).isTrue();
            assertThat(maybe.isSome()).isFalse();
        }

        @Test
        @DisplayName("is same as none()")
        void empty_isSameAsNone() {
            Maybe<String> none = Maybe.none();
            Maybe<String> empty = Maybe.empty();

            assertThat(none).isSameAs(empty);
        }
    }

    @Nested
    @DisplayName("tryFirst()")
    class TryFirstMethod {

        @Test
        @DisplayName("with non-empty list returns Some")
        void withNonEmptyList_returnsSome() {
            List<String> list = Arrays.asList("a", "b", "c");
            Maybe<String> maybe = Maybe.tryFirst(list);

            assertThat(maybe.isSome()).isTrue();
            assertThat(maybe.getValueOrThrow()).isEqualTo("a");
        }

        @Test
        @DisplayName("with empty list returns None")
        void withEmptyList_returnsNone() {
            List<String> list = new ArrayList<>();
            Maybe<String> maybe = Maybe.tryFirst(list);

            assertThat(maybe.isNone()).isTrue();
        }

        @Test
        @DisplayName("with null list returns None")
        void withNullList_returnsNone() {
            Maybe<String> maybe = Maybe.tryFirst(null);

            assertThat(maybe.isNone()).isTrue();
        }
    }

    @Nested
    @DisplayName("tryLast()")
    class TryLastMethod {

        @Test
        @DisplayName("with non-empty list returns Some")
        void withNonEmptyList_returnsSome() {
            List<String> list = Arrays.asList("a", "b", "c");
            Maybe<String> maybe = Maybe.tryLast(list);

            assertThat(maybe.isSome()).isTrue();
            assertThat(maybe.getValueOrThrow()).isEqualTo("c");
        }

        @Test
        @DisplayName("with empty list returns None")
        void withEmptyList_returnsNone() {
            List<String> list = new ArrayList<>();
            Maybe<String> maybe = Maybe.tryLast(list);

            assertThat(maybe.isNone()).isTrue();
        }

        @Test
        @DisplayName("with null list returns None")
        void withNullList_returnsNone() {
            Maybe<String> maybe = Maybe.tryLast(null);

            assertThat(maybe.isNone()).isTrue();
        }
    }

    @Nested
    @DisplayName("isSome()")
    class IsSomeMethod {

        @Test
        @DisplayName("returns true for Some")
        void isSome_withSome_returnsTrue() {
            Maybe<String> maybe = Maybe.some("value");

            assertThat(maybe.isSome()).isTrue();
        }

        @Test
        @DisplayName("returns false for None")
        void isSome_withNone_returnsFalse() {
            Maybe<String> maybe = Maybe.none();

            assertThat(maybe.isSome()).isFalse();
        }
    }

    @Nested
    @DisplayName("isNone()")
    class IsNoneMethod {

        @Test
        @DisplayName("returns true for None")
        void isNone_withNone_returnsTrue() {
            Maybe<String> maybe = Maybe.none();

            assertThat(maybe.isNone()).isTrue();
        }

        @Test
        @DisplayName("returns false for Some")
        void isNone_withSome_returnsFalse() {
            Maybe<String> maybe = Maybe.some("value");

            assertThat(maybe.isNone()).isFalse();
        }
    }

    @Nested
    @DisplayName("isDefined()")
    class IsDefinedMethod {

        @Test
        @DisplayName("returns true for Some")
        void isDefined_withSome_returnsTrue() {
            Maybe<String> maybe = Maybe.some("value");

            assertThat(maybe.isDefined()).isTrue();
        }

        @Test
        @DisplayName("returns false for None")
        void isDefined_withNone_returnsFalse() {
            Maybe<String> maybe = Maybe.none();

            assertThat(maybe.isDefined()).isFalse();
        }

        @Test
        @DisplayName("is alias for isSome()")
        void isDefined_isAliasForIsSome() {
            Maybe<String> some = Maybe.some("value");
            Maybe<String> none = Maybe.none();

            assertThat(some.isDefined()).isEqualTo(some.isSome());
            assertThat(none.isDefined()).isEqualTo(none.isSome());
        }
    }

    @Nested
    @DisplayName("getValueOrThrow()")
    class GetValueOrThrowMethod {

        @Test
        @DisplayName("returns value for Some")
        void getValueOrThrow_withSome_returnsValue() {
            Maybe<String> maybe = Maybe.some("test");

            String value = maybe.getValueOrThrow();

            assertThat(value).isEqualTo("test");
        }

        @Test
        @DisplayName("throws NoSuchElementException for None")
        void getValueOrThrow_withNone_throwsException() {
            Maybe<String> maybe = Maybe.none();

            assertThatThrownBy(maybe::getValueOrThrow)
                    .isInstanceOf(NoSuchElementException.class);
        }
    }

    @Nested
    @DisplayName("getValueOrThrow(Supplier)")
    class GetValueOrThrowWithMessageMethod {

        @Test
        @DisplayName("returns value for Some")
        void getValueOrThrow_withSome_returnsValue() {
            Maybe<String> maybe = Maybe.some("test");

            String value = maybe.getValueOrThrow(() -> "custom message");

            assertThat(value).isEqualTo("test");
        }

        @Test
        @DisplayName("throws NoSuchElementException with custom message for None")
        void getValueOrThrow_withNone_throwsExceptionWithMessage() {
            Maybe<String> maybe = Maybe.none();

            assertThatThrownBy(() -> maybe.getValueOrThrow(() -> "custom error message"))
                    .isInstanceOf(NoSuchElementException.class)
                    .hasMessage("custom error message");
        }
    }

    @Nested
    @DisplayName("getValueOrDefault(T)")
    class GetValueOrDefaultMethod {

        @Test
        @DisplayName("returns value for Some")
        void getValueOrDefault_withSome_returnsValue() {
            Maybe<String> maybe = Maybe.some("test");

            String value = maybe.getValueOrDefault("default");

            assertThat(value).isEqualTo("test");
        }

        @Test
        @DisplayName("returns default value for None")
        void getValueOrDefault_withNone_returnsDefault() {
            Maybe<String> maybe = Maybe.none();

            String value = maybe.getValueOrDefault("default");

            assertThat(value).isEqualTo("default");
        }
    }

    @Nested
    @DisplayName("getValueOrDefault(Supplier)")
    class GetValueOrDefaultSupplierMethod {

        @Test
        @DisplayName("returns value for Some without calling supplier")
        void getValueOrDefault_withSome_returnsValueWithoutCallingSupplier() {
            Maybe<String> maybe = Maybe.some("test");
            boolean[] supplierCalled = {false};

            String value = maybe.getValueOrDefault(() -> {
                supplierCalled[0] = true;
                return "default";
            });

            assertThat(value).isEqualTo("test");
            assertThat(supplierCalled[0]).isFalse();
        }

        @Test
        @DisplayName("calls supplier and returns value for None")
        void getValueOrDefault_withNone_callsSupplier() {
            Maybe<String> maybe = Maybe.none();

            String value = maybe.getValueOrDefault(() -> "default");

            assertThat(value).isEqualTo("default");
        }
    }

    @Nested
    @DisplayName("toOptional()")
    class ToOptionalMethod {

        @Test
        @DisplayName("returns Optional with value for Some")
        void toOptional_withSome_returnsOptionalWithValue() {
            Maybe<String> maybe = Maybe.some("test");

            Optional<String> optional = maybe.toOptional();

            assertThat(optional).isPresent().hasValue("test");
        }

        @Test
        @DisplayName("returns empty Optional for None")
        void toOptional_withNone_returnsEmptyOptional() {
            Maybe<String> maybe = Maybe.none();

            Optional<String> optional = maybe.toOptional();

            assertThat(optional).isEmpty();
        }
    }

    @Nested
    @DisplayName("equals()")
    class EqualsMethod {

        @Test
        @DisplayName("returns true for same instance")
        void equals_sameInstance_returnsTrue() {
            Maybe<String> maybe = Maybe.some("test");

            assertThat(maybe).isSameAs(maybe);
        }

        @Test
        @DisplayName("returns true for equal Some values")
        void equals_equalSomeValues_returnsTrue() {
            Maybe<String> maybe1 = Maybe.some("test");
            Maybe<String> maybe2 = Maybe.some("test");

            assertThat(maybe1).isEqualTo(maybe2);
        }

        @Test
        @DisplayName("returns false for different Some values")
        void equals_differentSomeValues_returnsFalse() {
            Maybe<String> maybe1 = Maybe.some("test1");
            Maybe<String> maybe2 = Maybe.some("test2");

            assertThat(maybe1).isNotEqualTo(maybe2);
        }

        @Test
        @DisplayName("returns true for None instances")
        void equals_noneInstances_returnsTrue() {
            Maybe<String> none1 = Maybe.none();
            Maybe<String> none2 = Maybe.none();

            assertThat(none1).isEqualTo(none2);
        }

        @Test
        @DisplayName("returns false when comparing Some with None")
        void equals_someAndNone_returnsFalse() {
            Maybe<String> some = Maybe.some("test");
            Maybe<String> none = Maybe.none();

            assertThat(some).isNotEqualTo(none);
        }

        @Test
        @DisplayName("returns false for null")
        void equals_null_returnsFalse() {
            Maybe<String> maybe = Maybe.some("test");

            assertThat(maybe).isNotNull();
        }

        @Test
        @DisplayName("returns false for different type")
        void equals_differentType_returnsFalse() {
            Maybe<String> maybe = Maybe.some("test");

            assertThat(maybe).isNotEqualTo("test");
        }
    }

    @Nested
    @DisplayName("hashCode()")
    class HashCodeMethod {

        @Test
        @DisplayName("returns same hash for equal Maybes")
        void hashCode_equalMaybes_returnSameHash() {
            Maybe<String> maybe1 = Maybe.some("test");
            Maybe<String> maybe2 = Maybe.some("test");

            assertThat(maybe1.hashCode()).isEqualTo(maybe2.hashCode());
        }

        @Test
        @DisplayName("returns same hash for None instances")
        void hashCode_noneInstances_returnSameHash() {
            Maybe<String> none1 = Maybe.none();
            Maybe<String> none2 = Maybe.none();

            assertThat(none1.hashCode()).isEqualTo(none2.hashCode());
        }
    }

    @Nested
    @DisplayName("toString()")
    class ToStringMethod {

        @Test
        @DisplayName("returns Some{value} for Some")
        void toString_withSome_returnsSomeFormat() {
            Maybe<String> maybe = Maybe.some("hello");

            assertThat(maybe.toString()).isEqualTo("Some{hello}");
        }

        @Test
        @DisplayName("returns None{} for None")
        void toString_withNone_returnsNoneFormat() {
            Maybe<String> maybe = Maybe.none();

            assertThat(maybe.toString()).isEqualTo("None{}");
        }
    }
}
