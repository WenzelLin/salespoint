package org.salespointframework.core.accountancy;

import java.util.Objects;

import org.salespointframework.core.accountancy.payment.PaymentMethod;
import org.salespointframework.core.money.Money;
import org.salespointframework.core.order.OrderIdentifier;
import org.salespointframework.core.user.UserIdentifier;

/**
 * 
 * @author Paul Henke
 * 
 */
public class TransientProductPaymentEntry extends TransientAccountancyEntry
{
	/**
	 * The {@link OrderIdentifier} to which this
	 * <code>TransientProductPaymentEntry</code> refers to.
	 */
	private final OrderIdentifier orderIdentifier;

	/**
	 * The {@link UserIdentifier} to which this
	 * <code>TransientProductPaymentEntry</code> refers to.
	 */
	private final UserIdentifier userIdentifier;

	private final PaymentMethod paymentMethod;


	// TODO ergänzen * @param paymentMethod 
	
	/**
	 * A <code>TransientProductPaymentEntry</code> is constructed for a specific
	 * {@link OrderIdentifier} attached to it. This Entry saves also the
	 * {@link UserIdentifier} and the specified amount that was payed.
	 * 
	 * @param orderIdentifier
	 *            the {@link OrderIdentifier} to which this
	 *            <code>ProductPaymentEntry</code> will refer to.
	 * @param userIdentifier
	 *            the {@link UserIdentifier} to which this
	 *            <code>ProductPaymentEntry</code> will refer to.
	 * @param amount
	 *            the {@link Money} that was payed.
	 * @param description
	 *            textual description of the payment entry
	 * @param paymentMethod 
	 */
	
	public TransientProductPaymentEntry(OrderIdentifier orderIdentifier,
			UserIdentifier userIdentifier, Money amount, String description,
			PaymentMethod paymentMethod) {
		super(amount, description);
		this.orderIdentifier = Objects.requireNonNull(orderIdentifier,
				"orderIdentifier must not be null");
		this.userIdentifier = Objects.requireNonNull(userIdentifier,
				"userIdentifier must not be null");
		this.paymentMethod = Objects.requireNonNull(paymentMethod,
				"paymentMethod must not be null");
	}

	/**
	 * @return the {@link UserIdentifier}, to which this payment refers to
	 */
	public final UserIdentifier getUserIdentifier() {
		return userIdentifier;
	}

	/**
	 * @return the {@link OrderIdentifier}, to which this payment refers to
	 */
	public final OrderIdentifier getOrderIdentifier() {
		return orderIdentifier;
	}

	/**
	 * 
	 * @return the {@link PaymentMethod} chosen for the order belonging to
	 *         this <code>ProductPaymentEntry</code>
	 */
	public final PaymentMethod getPaymentMethod() {
		return paymentMethod;
	}
}