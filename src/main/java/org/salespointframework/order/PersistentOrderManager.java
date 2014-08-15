package org.salespointframework.order;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.salespointframework.accountancy.Accountancy;
import org.salespointframework.catalog.ProductIdentifier;
import org.salespointframework.inventory.Inventory;
import org.salespointframework.inventory.InventoryItem;
import org.salespointframework.order.OrderCompletionResult.OrderCompletionStatus;
import org.salespointframework.quantity.Quantity;
import org.salespointframework.time.BusinessTime;
import org.salespointframework.useraccount.UserAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 
 * @author Thomas Dedek
 * @author Paul Henke
 * @author Oliver Gierke
 */
@Service
@Transactional
class PersistentOrderManager implements OrderManager {

	private final Inventory inventory;
	private final TransactionTemplate txTemplate;
	private final Accountancy accountancy;
	private final BusinessTime businessTime;

	@PersistenceContext
	private EntityManager entityManager;
	
	/**
	 * creates a new PersistentOrderManager
	 */
	@Autowired
	public PersistentOrderManager(Inventory inventory, Accountancy accountancy, BusinessTime businessTime, PlatformTransactionManager txManager) {
		
		this.inventory = Objects.requireNonNull(inventory, "inventory must not be null!");
		this.accountancy = Objects.requireNonNull(accountancy, "accountancy must not be null!");
		this.businessTime = Objects.requireNonNull(businessTime, "businessTime must not be null!");
		this.txTemplate = new TransactionTemplate(txManager);
	}
	
	@Override
	public void add(Order order)
	{
		Objects.requireNonNull(order, "order must be not null");
		if(order.getDateCreated() == null) {
			order.setDateCreated(businessTime.getTime());
		}
		entityManager.persist(order);
	}
	
	/**
	 * Adds multiple {@link Order}s to this PersistentOrderManager
	 * 
	 * @param orders
	 *            an {@link Iterable} of {@link Order}s to be added
	 * 
	 * @throws NullPointerException if orders is null
	 */
	public void addAll(Iterable<? extends Order> orders) {
		Objects.requireNonNull(orders, "orders must not be null");
		for(Order order : orders) {
			entityManager.persist(order);
		}
	}
	

	@Override
	public final <T extends Order> T get(Class<T> clazz, OrderIdentifier orderIdentifier)
	{
		Objects.requireNonNull(orderIdentifier, "orderIdentifier must not be null");
		return entityManager.find(clazz, orderIdentifier);
	}

	@Override
	public final boolean contains(OrderIdentifier orderIdentifier)
	{
		Objects.requireNonNull(orderIdentifier, "orderIdentifier must not be null");
		return entityManager.find(Order.class, orderIdentifier) != null;
	}

	@Override
	public final <T extends Order> Iterable<T> find(Class<T> clazz, LocalDateTime from, LocalDateTime to)
	{
		Objects.requireNonNull(from, "from must not be null");
		Objects.requireNonNull(to, "to must not be null");

		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<T> cq = cb.createQuery(clazz);
		Root<T> entry = cq.from(clazz);
		cq.where(cb.between(entry.get(Order_.dateCreated), from, to));
		TypedQuery<T> tq = entityManager.createQuery(cq);

		return tq.getResultList();
	}

	@Override
	public final <T extends Order> Iterable<T> find(Class<T> clazz, OrderStatus orderStatus)
	{
		Objects.requireNonNull(clazz, "clazz must not be null");
		Objects.requireNonNull(orderStatus, "orderStatus must not be null");

		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<T> cq = cb.createQuery(clazz);
		Root<T> entry = cq.from(clazz);
		cq.where(cb.equal(entry.get(Order_.orderStatus), orderStatus));
		TypedQuery<T> tq = entityManager.createQuery(cq);

		return tq.getResultList();
	}

	@Override
	public final <T extends Order> Iterable<T> find(Class<T> clazz, UserAccount userAccount)
	{
		Objects.requireNonNull(clazz, "clazz must not be null");
		Objects.requireNonNull(userAccount, "userAccount must not be null");

		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<T> cq = cb.createQuery(clazz);
		Root<T> entry = cq.from(clazz);
		cq.where(cb.equal(entry.get(Order_.userAccount), userAccount));
		TypedQuery<T> tq = entityManager.createQuery(cq);

		return tq.getResultList();
	}

	@Override
	public final <T extends Order> Iterable<T> find(Class<T> clazz, UserAccount userAccount, LocalDateTime from, LocalDateTime to)
	{
		Objects.requireNonNull(userAccount, "userAccount must not be null");
		Objects.requireNonNull(from, "from must not be null");
		Objects.requireNonNull(to, "to must not be null");

		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<T> cq = cb.createQuery(clazz);
		Root<T> entry = cq.from(clazz);
		Predicate p1 = cb.equal(entry.get(Order_.userAccount), userAccount);
		Predicate p2 = cb.between(entry.get(Order_.dateCreated), from, to);
		cq.where(p1, p2);
		TypedQuery<T> tq = entityManager.createQuery(cq);

		return tq.getResultList();
	}


	@Override
	public final void update(Order order)
	{
		Objects.requireNonNull(order, "order must not be null");
		entityManager.merge(order);
	}
	

	@Override
	public OrderCompletionResult completeOrder(final Order order) {

		if(order.getOrderStatus() != OrderStatus.PAYED)  {
			return new InternalOrderCompletionResult(OrderCompletionStatus.FAILED);
		}
		
		Objects.requireNonNull(order, "order must not be null");
		
		final Map<InventoryItem, Quantity> goodItems = new HashMap<>();
		final Map<InventoryItem, Quantity> badItems = new HashMap<>();

		Iterable<OrderLine> lineItems = order.getOrderLines();
		
		for (OrderLine orderline : lineItems) {
			ProductIdentifier productIdentifier = orderline
					.getProductIdentifier();
			InventoryItem inventoryItem = inventory
					.getByProductIdentifier(InventoryItem.class,
							productIdentifier);

			// TODO was machen wenn nicht im Inventar
			if (inventoryItem == null) {
				System.out.println("No InventoryItem with given ProductIndentifier found in PersistentInventory. Have you initialized your PersistentInventory? Do you need to re-stock your Inventory?");
				break;
			}
			if (!inventoryItem.getQuantity().subtract(orderline.getQuantity())
					.isNegative()) {
				goodItems.put(inventoryItem, orderline.getQuantity());
			} else {
				badItems.put(inventoryItem, orderline.getQuantity());
			}
		}
		
		return txTemplate.execute(new TransactionCallback<OrderCompletionResult>() {
			
			@Override
			public OrderCompletionResult doInTransaction(TransactionStatus status) {

				if (goodItems.size() != order.getNumberOfLineItems()) {
					
					status.setRollbackOnly();
					
					System.out.println("Number of items requested by the OrderLine is greater than the number available in the Inventory. Please re-stock.");
					return new InternalOrderCompletionResult(
							OrderCompletionStatus.FAILED);
				}
				
				System.out.println("Number of items requested by the OrderLine removed from the Inventory.");
				
				boolean failed = false;
				
				for (InventoryItem inventoryItem : goodItems.keySet()) {
					Quantity quantity = goodItems.get(inventoryItem);
					inventoryItem.decreaseQuantity(quantity);
					if (inventoryItem.getQuantity().isNegative()) {
						failed = true;
						break;
					}
				}

				// TODO DRY IT
				if (failed) {
					
					status.setRollbackOnly();
					return new InternalOrderCompletionResult(
							OrderCompletionStatus.FAILED);
				}
				
				order.complete();
				return new InternalOrderCompletionResult(
						OrderCompletionStatus.SUCCESSFUL);
			}
		});
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.salespointframework.order.OrderManager#pay(org.salespointframework.order.Order)
	 */
	@Override
	public boolean payOrder(Order order) {
		Objects.requireNonNull(order, "order must not be null");
		if (!order.isPaymentExpected()) {
			return false;
		}

		accountancy.add(order.markPaid());
		return true;
	}
	
	@Override
	public boolean cancelOrder(Order order) {
		Objects.requireNonNull(order, "order must not be null");
		if (order.getOrderStatus() == OrderStatus.OPEN) {
			order.cancel();
			return true;
		} else {
			return false;
		}
	}
	
	private final class InternalOrderCompletionResult implements OrderCompletionResult {
		private final OrderCompletionStatus status;

		public InternalOrderCompletionResult(OrderCompletionStatus status) {
			this.status = status;
		}

		@Override
		public OrderCompletionStatus getStatus() {
			return status;
		}
	}
}