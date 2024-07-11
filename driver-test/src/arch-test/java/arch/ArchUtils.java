package arch;

import com.arangodb.arch.NoRawTypesInspection;
import com.tngtech.archunit.base.ChainableFunction;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaType;
import com.tngtech.archunit.core.domain.properties.HasReturnType;
import com.tngtech.archunit.core.domain.properties.HasType;


class ArchUtils {

    static class JavaTypeExt {
        static DescribedPredicate<JavaType> rawTypes(DescribedPredicate<? super JavaClass> predicate) {
            return new DescribedPredicate<JavaType>("raw types " + predicate.getDescription()) {
                @Override
                public boolean test(JavaType t) {
                    if (t.toErasure().isAnnotatedWith(NoRawTypesInspection.class)) {
                        return predicate.test(t.toErasure());
                    } else {
                        return t.getAllInvolvedRawTypes().stream().allMatch(predicate);
                    }
                }
            };
        }
    }

    static class HasReturnTypeExt {
        private static final ChainableFunction<HasReturnType, JavaType> GET_RETURN_TYPE = new ChainableFunction<HasReturnType, JavaType>() {
            @Override
            public JavaType apply(HasReturnType input) {
                return input.getReturnType();
            }
        };

        static DescribedPredicate<HasReturnType> returnType(DescribedPredicate<? super JavaType> predicate) {
            return predicate.onResultOf(GET_RETURN_TYPE).as("return type %s", predicate.getDescription());
        }
    }

    static class HasTypeExt {
        private static final ChainableFunction<HasType, JavaType> GET_TYPE = new ChainableFunction<HasType, JavaType>() {
            @Override
            public JavaType apply(HasType input) {
                return input.getType();
            }
        };

        static DescribedPredicate<HasType> type(DescribedPredicate<? super JavaType> predicate) {
            return GET_TYPE.is(predicate).as("type " + predicate.getDescription());
        }

        static DescribedPredicate<HasType> rawTypes(DescribedPredicate<? super JavaClass> predicate) {
            return type(JavaTypeExt.rawTypes(predicate));
        }
    }

}
