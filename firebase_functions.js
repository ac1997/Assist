const admin = require('firebase-admin');
const functions = require('firebase-functions');

admin.initializeApp(functions.config().firebase);

var db = admin.firestore();

exports.onStatusChange = functions.firestore
    .document('requests/{requestId}')
    .onUpdate((change, context) => {

    	const data = change.after.data();
      	const previousData = change.before.data();

      	if (data.status === previousData.status) return null;

      	if (data.status === 1) {
			return admin.firestore().collection("tokens").doc(data.postedBy.uid).get().then(queryResult => {
				var message = {
					"notification": {
				        "title" : "Request accepted!", 
				        "body" : data.acceptedBy[0].name + " accepted your request " + data.title + ".",
				    }, 
				    "android": {
				       "notification": {
				       		"sound": "default",
				         	"click_action": ".RequestDetails"
				       }
				    },
				  	"data": {
				    	"requestId": context.params.requestId,
				    	"title": data.title,
				    	"acceptedByName": data.acceptedBy[0].name,
				    	"acceptedByUid": data.acceptedBy[0].uid,
				  	},
				  	"token": queryResult.data().tokenId
				};

				return admin.messaging().send(message)
				  	.then((response) => {
				    	console.log('Successfully sent message:', response);
				    	return response;
				  	})
				  	.catch((error) => {
				    	console.log('Error sending message:', error);
				    	throw error;
				  	});
			});
		}

		return null;
    });

exports.check_request_validity = functions.pubsub
  .topic('minute-tick')
  .onPublish((message) => {
  		return admin.firestore().collection("requests").where('status', '==', 0).get().then(queryResult => {
  			const now = Date.now() / 1000;

			queryResult.forEach(doc => {
            	if (now > doc.data().date + doc.data().duration * 60) {
            		admin.firestore().collection("requests").doc(doc.id).set({
            			status: 5,
            			isNow: false
            		}, {merge: true});
            	}

        	});
        	return true;
		});
  });

exports.check_request_completed = functions.pubsub
  .topic('five-minutes-tick')
  .onPublish((message) => {
  		return admin.firestore().collection("requests").where('status', '==', 1).get().then(queryResult => {
  			const now = Date.now() / 1000;

			queryResult.forEach(doc => {
            	if (now > doc.data().date + (doc.data().duration + 5) * 60) {
            		admin.firestore().collection("requests").doc(doc.id).set({
            			status: 2,
            			isNow: false
            		}, {merge: true});
            	}

        	});
        	return true;
		});
  });
