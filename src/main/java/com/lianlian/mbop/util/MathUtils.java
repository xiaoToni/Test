package com.lianlian.mbop.util;

import java.math.BigDecimal;

/**
 * 
 * @author zhuxl
 *
 */
public class MathUtils {

	public static long convertToTransfer(String realMoney, int multiply) {
		BigDecimal moenyDecimal = new BigDecimal(realMoney);
		return convertToTransfer(moenyDecimal, multiply);
	}

	public static long convertToTransfer(BigDecimal realMoney, int multiply) {
		BigDecimal rateDecimal = new BigDecimal(multiply);
		return realMoney.multiply(rateDecimal).longValue();
	}

	public static String transferToReal(long transferMoney, int divide) {
		BigDecimal moenyDecimal = new BigDecimal(Long.toString(transferMoney));
		BigDecimal rateDecimal = new BigDecimal(divide);
		BigDecimal result = moenyDecimal.divide(rateDecimal, 2, BigDecimal.ROUND_DOWN);
		return result.toString();
	}

}
