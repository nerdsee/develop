package org.stoevesand.findow.persistence;

import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import org.stoevesand.finapi.AccountsService;
import org.stoevesand.finapi.model.Category;
import org.stoevesand.findow.model.Account;
import org.stoevesand.findow.model.CategorySum;
import org.stoevesand.findow.model.ErrorHandler;
import org.stoevesand.findow.model.Transaction;
import org.stoevesand.findow.model.User;

public class PersistanceManager {
	private EntityManagerFactory entityManagerFactory;

	private static PersistanceManager _instance = null;

	// private EntityManager entityManager;

	public static PersistanceManager getInstance() {
		if (_instance == null) {
			_instance = new PersistanceManager();
		}
		return _instance;
	}

	private PersistanceManager() {
		entityManagerFactory = Persistence.createEntityManagerFactory("org.stoevesand.finapi.persistence");
	}

	public void storeTx(List<Transaction> transactionList) {
		// create a couple of events...
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		entityManager.getTransaction().begin();

		for (Transaction t : transactionList) {
			System.out.println(t);
			entityManager.persist(t);
		}

		entityManager.getTransaction().commit();
		entityManager.close();
	}

	public void storeAccounts(List<Account> accountList) {
		// create a couple of events...
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		entityManager.getTransaction().begin();

		for (Account t : accountList) {
			System.out.println(t);
			entityManager.persist(t);
		}

		entityManager.getTransaction().commit();
		entityManager.close();
	}

	public List<Transaction> getTx(User user, long accountId, int days) throws ErrorHandler {

		// create a couple of events...
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		entityManager.getTransaction().begin();

		List<Transaction> result = new Vector<Transaction>();

		if (accountId > 0) {
			// TODO: wieder heil machen
			Account account = user.getAccount(accountId);
			// Standardfall mit echter accountId
			// Account account = entityManager.find(Account.class, accountId);
			if (account != null) {
				List<Transaction> subResult = entityManager.createQuery("select t from Transaction t where t.accountId=:aid and t.bookingDate > current_date - :daydelta", Transaction.class).setParameter("daydelta", days).setParameter("aid", account.getSourceId()).getResultList();
				result.addAll(subResult);
			} else {
				throw new ErrorHandler(500, "NO SUCH ACCOUNT");
			}

		} else {
			// wenn keine accountId angegeben ist, dann werden die Tx von allen
			// Accounts geladen
			List<Account> accounts = entityManager.createQuery("select a from Account a where user=:id", Account.class).setParameter("id", user).getResultList();
			if (accounts != null) {
				for (Account account : accounts) {
					List<Transaction> subResult = entityManager.createQuery("select t from Transaction t where t.accountId=:aid and t.bookingDate > current_date - :daydelta", Transaction.class).setParameter("daydelta", days).setParameter("aid", account.getSourceId()).getResultList();
					result.addAll(subResult);
				}
			}
		}

		entityManager.getTransaction().commit();
		entityManager.close();

		return result;
	}

	public User getUserByName(String id) {
		User ret = null;
		// create a couple of events...
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		entityManager.getTransaction().begin();
		List<User> result = entityManager.createQuery("select t from User t where t.name = :username").setParameter("username", id).getResultList();
		if (result.size() > 0) {
			ret = (User) result.get(0);
		}
		entityManager.getTransaction().commit();
		entityManager.close();

		return ret;
	}

	public User getUserByExternalName(String id) {
		User ret = null;
		// create a couple of events...
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		entityManager.getTransaction().begin();
		List<User> result = entityManager.createQuery("select t from User t where t.backendName = :username", User.class).setParameter("username", id).getResultList();
		if (result.size() > 0) {
			ret = (User) result.get(0);
		}
		entityManager.getTransaction().commit();
		entityManager.close();

		return ret;
	}

	public void store(Object obj) {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		entityManager.getTransaction().begin();
		entityManager.persist(obj);
		entityManager.getTransaction().commit();
	}

	public void remove(EntityManager em, Object entity) {
		em.remove(em.contains(entity) ? entity : em.merge(entity));
	}

	public void persist(EntityManager em, Object entity) {
		em.persist(em.contains(entity) ? entity : em.merge(entity));
	}

	public Transaction getTxByExternalId(Long sourceId) {
		Transaction ret = null;

		EntityManager entityManager = entityManagerFactory.createEntityManager();
		entityManager.getTransaction().begin();

		List<Transaction> result = entityManager.createQuery("select t from Transaction t where t.sourceId=:sourceid").setParameter("sourceid", sourceId).getResultList();
		if (result.size() > 0) {
			ret = (Transaction) result.get(0);
		}

		entityManager.getTransaction().commit();
		entityManager.close();

		return ret;
	}

	public Category getCategory(Category category) {

		EntityManager entityManager = entityManagerFactory.createEntityManager();
		entityManager.getTransaction().begin();

		Category ret = entityManager.find(Category.class, category.getId());

		if (ret == null) {
			entityManager.persist(category);
			ret = category;
		}

		entityManager.getTransaction().commit();
		entityManager.close();

		return ret;
	}

	public Category getCategory(long categoryId) {

		EntityManager entityManager = entityManagerFactory.createEntityManager();
		entityManager.getTransaction().begin();

		Category ret = entityManager.find(Category.class, categoryId);

		entityManager.getTransaction().commit();
		entityManager.close();

		return ret;
	}

	public List<CategorySum> getCategorySummary() {
		EntityManager entityManager = entityManagerFactory.createEntityManager();

		// Query q = entityManager.createNamedQuery("sumQuery",
		// CategorySum.class);
		Query q = entityManager.createNativeQuery("select coalesce(category_id,0) as category_id, count(*) as count, sum(amount) as sum from transactions t group by t.category_id", CategorySum.class);
		List<CategorySum> catsum = q.getResultList();

		entityManager.close();
		return catsum;
	}

	public void deleteUserByName(String id) {
		User ret = null;
		// create a couple of events...
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		entityManager.getTransaction().begin();
		List<User> result = entityManager.createQuery("select t from User t where t.name = :username", User.class).setParameter("username", id).getResultList();
		if (result.size() > 0) {
			ret = (User) result.get(0);
		}
		entityManager.remove(ret);
		entityManager.getTransaction().commit();
		entityManager.close();
	}

	public List<User> getUsers() {

		EntityManager entityManager = entityManagerFactory.createEntityManager();
		entityManager.getTransaction().begin();

		List<User> users = entityManager.createQuery("select u from User u", User.class).getResultList();

		entityManager.getTransaction().commit();
		entityManager.close();

		return users;
	}

	public List<Account> getAccounts(User user, String userToken) {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		entityManager.getTransaction().begin();

		List<Account> accounts = entityManager.createQuery("select a from Account a where a.user=:user", Account.class).setParameter("user", user).getResultList();

		for (Account account : accounts) {
			int ret = account.refresh(userToken);
			if (ret == 404) {
				entityManager.remove(account);
			} else {
				entityManager.persist(account);
			}
		}

		entityManager.getTransaction().commit();
		entityManager.close();

		return accounts;
	}

	public Account getAccount(User user, long accountId, String userToken) {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		entityManager.getTransaction().begin();
		Account retAccount = null;

		Account account = entityManager.find(Account.class, accountId);
		if (account != null) {
			int ret = account.refresh(userToken);
			if (ret == 404) {
				entityManager.remove(account);
			} else {
				entityManager.persist(account);
				retAccount = account;
			}
		}

		entityManager.getTransaction().commit();
		entityManager.close();

		return retAccount;
	}

	public void deleteAccount(User user, long accountId, String userToken) throws ErrorHandler {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		entityManager.getTransaction().begin();

		user.removeAccount(accountId);
		persist(entityManager, user);

		entityManager.getTransaction().commit();
		entityManager.close();
	}

	public void deleteAccounts(int connectionId) {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		entityManager.getTransaction().begin();

		List<Account> accounts = entityManager.createQuery("select a from Account a where a.bankConnectionId=:bcid", Account.class).setParameter("bcid", connectionId).getResultList();

		for (Account account : accounts) {
			entityManager.remove(account);
		}

		entityManager.getTransaction().commit();
		entityManager.close();
	}
}
