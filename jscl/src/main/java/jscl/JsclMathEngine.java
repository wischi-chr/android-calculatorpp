package jscl;

import jscl.math.Expression;
import jscl.math.Generic;
import jscl.math.NotIntegerException;
import jscl.math.function.*;
import jscl.math.operator.Operator;
import jscl.math.operator.Percent;
import jscl.math.operator.Rand;
import jscl.math.operator.matrix.OperatorsRegistry;
import jscl.text.ParseException;
import midpcalc.Real;
import org.solovyev.common.NumberFormatter;
import org.solovyev.common.math.MathRegistry;
import org.solovyev.common.msg.MessageRegistry;
import org.solovyev.common.msg.Messages;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.List;

import static midpcalc.Real.NumberFormat.*;

public class JsclMathEngine implements MathEngine {

    public static final AngleUnit DEFAULT_ANGLE_UNITS = AngleUnit.deg;
    public static final NumeralBase DEFAULT_NUMERAL_BASE = NumeralBase.dec;
    public static final char GROUPING_SEPARATOR_DEFAULT = ' ';
    public static final int MAX_FRACTION_DIGITS = 20;
    @Nonnull
    private static JsclMathEngine instance = new JsclMathEngine();
    @Nonnull
    private final ConstantsRegistry constantsRegistry = new ConstantsRegistry();
    @Nonnull
    private final ThreadLocal<NumberFormatter> numberFormatter = new ThreadLocal<NumberFormatter>() {
        @Override
        protected NumberFormatter initialValue() {
            return new NumberFormatter();
        }
    };
    private char groupingSeparator = GROUPING_SEPARATOR_DEFAULT;
    private boolean roundResult = false;
    private int numberFormat = FSE_NONE;
    private int precision = 5;
    private boolean useGroupingSeparator = false;
    @Nonnull
    private AngleUnit angleUnits = DEFAULT_ANGLE_UNITS;
    @Nonnull
    private NumeralBase numeralBase = DEFAULT_NUMERAL_BASE;
    @Nonnull
    private MessageRegistry messageRegistry = Messages.synchronizedMessageRegistry(new FixedCapacityListMessageRegistry(10));

    public JsclMathEngine() {
    }

    @Nonnull
    public static JsclMathEngine getInstance() {
        return instance;
    }

    private static int integerValue(final double value) throws NotIntegerException {
        if (Math.floor(value) == value) {
            return (int) value;
        } else {
            throw NotIntegerException.get();
        }
    }

    @Nonnull
    public String evaluate(@Nonnull String expression) throws ParseException {
        return evaluateGeneric(expression).toString();
    }

    @Nonnull
    public String simplify(@Nonnull String expression) throws ParseException {
        return simplifyGeneric(expression).toString();
    }

    @Nonnull
    public String elementary(@Nonnull String expression) throws ParseException {
        return elementaryGeneric(expression).toString();
    }

    @Nonnull
    public Generic evaluateGeneric(@Nonnull String expression) throws ParseException {
        if (expression.contains(Percent.NAME) || expression.contains(Rand.NAME)) {
            return Expression.valueOf(expression).numeric();
        } else {
            return Expression.valueOf(expression).expand().numeric();
        }
    }

    @Nonnull
    public Generic simplifyGeneric(@Nonnull String expression) throws ParseException {
        if (expression.contains(Percent.NAME) || expression.contains(Rand.NAME)) {
            return Expression.valueOf(expression);
        } else {
            return Expression.valueOf(expression).expand().simplify();
        }
    }

    @Nonnull
    public Generic elementaryGeneric(@Nonnull String expression) throws ParseException {
        return Expression.valueOf(expression).elementary();
    }

    @Nonnull
    public MathRegistry<Function> getFunctionsRegistry() {
        return FunctionsRegistry.getInstance();
    }

    @Nonnull
    public MathRegistry<Operator> getOperatorsRegistry() {
        return OperatorsRegistry.getInstance();
    }

    @Nonnull
    public MathRegistry<Operator> getPostfixFunctionsRegistry() {
        return PostfixFunctionsRegistry.getInstance();
    }

    @Nonnull
    public AngleUnit getAngleUnits() {
        return angleUnits;
    }

    public void setAngleUnits(@Nonnull AngleUnit angleUnits) {
        this.angleUnits = angleUnits;
    }

    @Nonnull
    public NumeralBase getNumeralBase() {
        return numeralBase;
    }

    public void setNumeralBase(@Nonnull NumeralBase numeralBase) {
        this.numeralBase = numeralBase;
    }

    @Nonnull
    public MathRegistry<IConstant> getConstantsRegistry() {
        return constantsRegistry;
    }

    @Nonnull
    public String format(@Nonnull Double value) throws NumeralBaseException {
        return format(value, numeralBase);
    }

    @Nonnull
    public String format(@Nonnull Double value, @Nonnull NumeralBase nb) throws NumeralBaseException {
        if (value.isInfinite()) {
            return formatInfinity(value);
        }
        if (value.isNaN()) {
            // return "NaN"
            return String.valueOf(value);
        }
        if (nb == NumeralBase.dec) {
            if (value == 0d) {
                return "0";
            }
            // detect if current number is precisely equals to constant in constants' registry  (NOTE: ONLY FOR SYSTEM CONSTANTS)
            final IConstant constant = findConstant(value);
            if (constant != null) {
                return constant.getName();
            }
        }
        final NumberFormatter nf = numberFormatter.get();
        nf.setGroupingSeparator(useGroupingSeparator ? groupingSeparator : NumberFormatter.NO_GROUPING);
        nf.setPrecision(roundResult ? precision : NumberFormatter.NO_ROUNDING);
        switch (numberFormat) {
            case Real.NumberFormat.FSE_ENG:
                nf.useEngineeringFormat(NumberFormatter.DEFAULT_MAGNITUDE);
                break;
            case FSE_SCI:
                nf.useScientificFormat(NumberFormatter.DEFAULT_MAGNITUDE);
                break;
            default:
                nf.useSimpleFormat();
                break;
        }
        return nf.format(value, nb.radix).toString();
    }

    @Nullable
    private IConstant findConstant(@Nonnull Double value) {
        final IConstant constant = findConstant(constantsRegistry.getSystemEntities(), value);
        if (constant != null) {
            return constant;
        }
        final IConstant piInv = constantsRegistry.get(Constants.PI_INV.getName());
        if (piInv != null && value.equals(piInv.getDoubleValue())) {
            return piInv;
        }
        return null;
    }

    private String formatInfinity(@Nonnull Double value) {
        // return predefined constant for infinity
        if (value >= 0) {
            return Constants.INF.getName();
        } else {
            return Constants.INF.expressionValue().negate().toString();
        }
    }

    @Nullable
    private IConstant findConstant(@Nonnull List<IConstant> constants, @Nonnull Double value) {
        for (int i = 0; i < constants.size(); i++) {
            final IConstant constant = constants.get(i);
            if (!value.equals(constant.getDoubleValue())) {
                continue;
            }
            final String name = constant.getName();
            if (name.equals(Constants.PI_INV.getName()) || name.equals(Constants.ANS)) {
                continue;
            }
            if (!name.equals(Constants.PI.getName()) || getAngleUnits() == AngleUnit.rad) {
                return constant;
            }
        }
        return null;
    }

    @Nonnull
    public String convert(@Nonnull Double value, @Nonnull NumeralBase to) {
        String ungroupedValue;
        try {
            // check if double can be converted to integer
            integerValue(value);

            ungroupedValue = to.toString(new BigDecimal(value).toBigInteger());
        } catch (NotIntegerException e) {
            ungroupedValue = to.toString(value, roundResult ? precision : MAX_FRACTION_DIGITS);
        }

        return addGroupingSeparators(to, ungroupedValue);
    }

    @Nonnull
    public MessageRegistry getMessageRegistry() {
        return messageRegistry;
    }

    public void setMessageRegistry(@Nonnull MessageRegistry messageRegistry) {
        this.messageRegistry = messageRegistry;
    }

    @Nonnull
    public String addGroupingSeparators(@Nonnull NumeralBase nb, @Nonnull String ungroupedDoubleValue) {
        if (useGroupingSeparator) {
            final String groupingSeparator = nb == NumeralBase.dec ? String.valueOf(this.groupingSeparator) : " ";

            final int dotIndex = ungroupedDoubleValue.indexOf(".");

            String ungroupedValue;
            if (dotIndex >= 0) {
                ungroupedValue = ungroupedDoubleValue.substring(0, dotIndex);
            } else {
                ungroupedValue = ungroupedDoubleValue;
            }
            // inject group separator in the resulted string
            // NOTE: space symbol is always used!!!
            StringBuilder result = insertSeparators(nb, groupingSeparator, ungroupedValue, true);

            result = result.reverse();

            if (dotIndex >= 0) {
                result.append(insertSeparators(nb, groupingSeparator, ungroupedDoubleValue.substring(dotIndex), false));
            }

            return result.toString();
        } else {
            return ungroupedDoubleValue;
        }
    }

    @Nonnull
    private StringBuilder insertSeparators(@Nonnull NumeralBase nb,
                                           @Nonnull String groupingSeparator,
                                           @Nonnull String value,
                                           boolean reversed) {
        final StringBuilder result = new StringBuilder(value.length() + nb.getGroupingSize() * groupingSeparator.length());

        if (reversed) {
            for (int i = value.length() - 1; i >= 0; i--) {
                result.append(value.charAt(i));
                if (i != 0 && (value.length() - i) % nb.getGroupingSize() == 0) {
                    result.append(groupingSeparator);
                }
            }
        } else {
            for (int i = 0; i < value.length(); i++) {
                result.append(value.charAt(i));
                if (i != 0 && i != value.length() - 1 && i % nb.getGroupingSize() == 0) {
                    result.append(groupingSeparator);
                }
            }
        }

        return result;
    }

    public void setRoundResult(boolean roundResult) {
        this.roundResult = roundResult;
    }

    public void setPrecision(int precision) {
        this.precision = precision;
    }

    public void setUseGroupingSeparator(boolean useGroupingSeparator) {
        this.useGroupingSeparator = useGroupingSeparator;
    }

    public void setScienceNotation(boolean scienceNotation) {
        setNumberFormat(scienceNotation ? FSE_SCI : FSE_NONE);
    }

    public void setNumberFormat(int numberFormat) {
        if (numberFormat != FSE_SCI && numberFormat != FSE_ENG && numberFormat != FSE_NONE) {
            throw new IllegalArgumentException("Unsupported format: " + numberFormat);
        }
        this.numberFormat = numberFormat;
    }

    public char getGroupingSeparator() {
        return this.groupingSeparator;
    }

    public void setGroupingSeparator(char groupingSeparator) {
        this.groupingSeparator = groupingSeparator;
    }
}
