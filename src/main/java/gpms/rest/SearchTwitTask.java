package gpms.rest;

import gpms.DAL.MongoDBConnector;
import gpms.dao.DelegationDAO;
import gpms.dao.NotificationDAO;
import gpms.dao.ProposalDAO;
import gpms.dao.UserAccountDAO;
import gpms.dao.UserProfileDAO;
import gpms.model.UserAccount;
import gpms.model.UserProfile;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.media.sse.OutboundEvent;
import org.glassfish.jersey.media.sse.SseBroadcaster;
import org.mongodb.morphia.Morphia;

import com.mongodb.MongoClient;

public class SearchTwitTask implements Runnable {

	private SseBroadcaster broadcaster;

	String userProfileID = new String();
	String userCollege = new String();
	String userDepartment = new String();
	String userPositionType = new String();
	String userPositionTitle = new String();
	Boolean userIsAdmin = false;

	MongoClient mongoClient = null;
	Morphia morphia = null;
	String dbName = "db_gpms";
	UserAccountDAO userAccountDAO = null;
	UserProfileDAO userProfileDAO = null;
	ProposalDAO proposalDAO = null;
	DelegationDAO delegationDAO = null;
	NotificationDAO notificationDAO = null;

	DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

	public SearchTwitTask(SseBroadcaster broadcaster, String userProfileID,
			String userCollege, String userDepartment, String userPositionType,
			String userPositionTitle, Boolean userIsAdmin) {
		this.broadcaster = broadcaster;
		this.userProfileID = userProfileID;
		this.userCollege = userCollege;
		this.userDepartment = userDepartment;
		this.userPositionType = userPositionType;
		this.userPositionTitle = userPositionTitle;
		this.userIsAdmin = userIsAdmin;

		mongoClient = MongoDBConnector.getMongo();
		morphia = new Morphia();
		morphia.map(UserProfile.class).map(UserAccount.class);
		userAccountDAO = new UserAccountDAO(mongoClient, morphia, dbName);
		userProfileDAO = new UserProfileDAO(mongoClient, morphia, dbName);
		proposalDAO = new ProposalDAO(mongoClient, morphia, dbName);
		delegationDAO = new DelegationDAO(mongoClient, morphia, dbName);
		notificationDAO = new NotificationDAO(mongoClient, morphia, dbName);
	}

	@Override
	public void run() {
		long notificationCount = 0L;
		try {
			notificationCount = notificationDAO.findAllNotificationCountAUser(
					userProfileID, userCollege, userDepartment,
					userPositionType, userPositionTitle, userIsAdmin);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		OutboundEvent.Builder eventBuilder = new OutboundEvent.Builder();
		// OutboundEvent event = eventBuilder.name("notification")
		// .mediaType(MediaType.TEXT_PLAIN_TYPE)
		// // .id(UUID.randomUUID().toString())
		// .data(String.class, notificationCount).build();

		// WARNING: IF I SET THE NAME OF THE EVENT IT DOES NOT WORK
		// eventBuilder.name("message");

		eventBuilder.mediaType(MediaType.APPLICATION_JSON_TYPE);
		eventBuilder.data(String.class, notificationCount);
		OutboundEvent event = eventBuilder.build();

		broadcaster.broadcast(event);

	}

}
