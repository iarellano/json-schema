/*
 * Original work Copyright (C) 2011 Everit Kft. (http://www.everit.org)
 * Modified work Copyright (c) 2019 Isaias Arellano - isaias.arellano.delgado@gmail.com
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
 */
package org.everit.json.schema;

import static java.lang.String.format;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.everit.json.schema.i18n.ResourceBundleThreadLocal;

class NumberSchemaValidatingVisitor extends Visitor {

    private static final List<Class<?>> INTEGRAL_TYPES = Arrays.asList(Integer.class, Long.class, BigInteger.class,
            AtomicInteger.class, AtomicLong.class);
    
    private final Object subject;

    private final ValidatingVisitor owner;

    private boolean exclusiveMinimum;

    private boolean exclusiveMaximum;

    private Number numberSubject;

    NumberSchemaValidatingVisitor(Object subject, ValidatingVisitor owner) {
        this.subject = subject;
        this.owner= owner;
    }

    @Override void visitNumberSchema(NumberSchema numberSchema) {
        if (owner.passesTypeCheck(Number.class, numberSchema.isRequiresNumber(), numberSchema.isNullable())) {
            if (!INTEGRAL_TYPES.contains(subject.getClass()) && numberSchema.requiresInteger()) {
                owner.failure(Integer.class, subject);
            } else {
                this.numberSubject = ((Number) subject);
                super.visitNumberSchema(numberSchema);
            }
        }
    }

    @Override void visitExclusiveMinimum(boolean exclusiveMinimum) {
        this.exclusiveMinimum = exclusiveMinimum;
    }

    @Override void visitMinimum(Number minimum) {
        if (minimum == null) {
            return;
        }
        if (exclusiveMinimum && compareNumber(numberSubject, minimum) <= 0) {
            owner.failure(format(ResourceBundleThreadLocal.get().getString("number.exclusiveMinimum.x_ngt_y"), subject, minimum), "exclusiveMinimum");
        } else if (compareNumber(numberSubject, minimum) < 0) {
            owner.failure(format(ResourceBundleThreadLocal.get().getString("number.minimum.x_ngte_y"), subject, minimum), "minimum");
        }
    }

    @Override void visitExclusiveMinimumLimit(Number exclusiveMinimumLimit) {
        if (exclusiveMinimumLimit != null) {
            if (compareNumber(numberSubject, exclusiveMinimumLimit) <= 0) {
                owner.failure(format(ResourceBundleThreadLocal.get().getString("number.exclusiveMinimum.x_ngt_y"), subject, exclusiveMinimumLimit), "exclusiveMinimum");
            }
        }
    }

    @Override void visitMaximum(Number maximum) {
        if (maximum == null) {
            return;
        }
        if (exclusiveMaximum && compareNumber(maximum, numberSubject) <= 0) {
            owner.failure(format(ResourceBundleThreadLocal.get().getString("number.exclusiveMaximum.x_nlt_y"), subject, maximum), "exclusiveMaximum");
        } else if (compareNumber(maximum, numberSubject) < 0) {
            owner.failure(format(ResourceBundleThreadLocal.get().getString("number.maximum.x_nlte_y"), subject, maximum), "maximum");
        }
    }

    @Override void visitExclusiveMaximum(boolean exclusiveMaximum) {
        this.exclusiveMaximum = exclusiveMaximum;
    }

    @Override void visitExclusiveMaximumLimit(Number exclusiveMaximumLimit) {
        if (exclusiveMaximumLimit != null) {
            if (compareNumber(numberSubject, exclusiveMaximumLimit) >= 0) {
                owner.failure(format(ResourceBundleThreadLocal.get().getString("number.exclusiveMaximum.nlt_y"), exclusiveMaximumLimit), "exclusiveMaximum");
            }
        }
    }

    @Override void visitMultipleOf(Number multipleOf) {
        if (multipleOf != null) {
            if (numberSubject instanceof BigDecimal) {
                BigDecimal remainder = ((BigDecimal) numberSubject).remainder(BigDecimal.valueOf(multipleOf.doubleValue()));
                if (remainder.compareTo(BigDecimal.ZERO) != 0) {
                    owner.failure(format(ResourceBundleThreadLocal.get().getString("number.multipleOf"), subject, multipleOf), "multipleOf");
                }
            }
            if (numberSubject instanceof BigInteger) {
                BigDecimal remainder = BigDecimal.valueOf(((BigInteger) numberSubject).longValue()).remainder(BigDecimal.valueOf(multipleOf.doubleValue()));
                if (remainder.compareTo(BigDecimal.ZERO) != 0) {
                    owner.failure(format(ResourceBundleThreadLocal.get().getString("number.multipleOf"), subject, multipleOf), "multipleOf");
                }
            }
            if (numberSubject instanceof Double) {
                BigDecimal remainder = BigDecimal.valueOf((Double) numberSubject).remainder(BigDecimal.valueOf(multipleOf.doubleValue()));
                if (remainder.compareTo(BigDecimal.ZERO) != 0) {
                    owner.failure(format(ResourceBundleThreadLocal.get().getString("number.multipleOf"), subject, multipleOf), "multipleOf");
                }
            }
            if (numberSubject instanceof Long) {
                BigDecimal remainder = BigDecimal.valueOf((Long) numberSubject).remainder(BigDecimal.valueOf(multipleOf.doubleValue()));
                if (remainder.compareTo(BigDecimal.ZERO) != 0) {
                    owner.failure(format(ResourceBundleThreadLocal.get().getString("number.multipleOf"), subject, multipleOf), "multipleOf");
                }
            }
            if (numberSubject instanceof Integer) {
                BigDecimal remainder = BigDecimal.valueOf((Integer) numberSubject).remainder(BigDecimal.valueOf(multipleOf.doubleValue()));
                if (remainder.compareTo(BigDecimal.ZERO) != 0) {
                    owner.failure(format(ResourceBundleThreadLocal.get().getString("number.multipleOf"), subject, multipleOf), "multipleOf");
                }
            }
        }
    }

    private int compareNumber(Number n1, Number n2) {

        BigDecimal number1 = n1 instanceof BigDecimal ? (BigDecimal) n1 : toBigDecimal(n1);
        BigDecimal number2 = n2 instanceof BigDecimal ? (BigDecimal) n2 : toBigDecimal(n2);
        return number1.compareTo(number2);
    }

    private BigDecimal toBigDecimal(Number number) {
        if (number instanceof Double) {
            return new BigDecimal((Double) number);
        }
        if (number instanceof Float) {
            return new BigDecimal((Float) number);
        }
        if (number instanceof Integer) {
            return new BigDecimal(new Long(((Integer)number).longValue()));
        }
        if (number instanceof Long) {
            return new BigDecimal((Long) number);
        }
        if (number instanceof BigInteger) {
            return BigDecimal.valueOf(((BigInteger) number).longValue());
        }
        return null;
    }
}
