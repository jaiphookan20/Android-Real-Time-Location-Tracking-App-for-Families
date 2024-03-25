require("firebase-functions/logger/compat");

const functions = require("firebase-functions")
const admin = require('firebase-admin');
admin.initializeApp();

// exports.createUserDoc = functions
//     .region("australia-southeast1")
//     .auth
//     .user()
//     .onCreate((user) => {
//         admin
//             .firestore()
//             .collection("users")
//             .doc(user.uid)
//             .set({
//                 circlesJoined: [],
//                 email: user.email || "",
//                 profileImage: user.photoUrl || "",
//                 userID: user.uid,
//                 username: user.displayName || "",
//             })
//             .then(() => {
//                 console.log("Doc written: ", user);
//                 return;
//             })
//             .catch((err) => {
//                 console.log(err);
//                 return;
//             })
//     });

exports.alertUsers = functions
    .region("australia-southeast1")
    .firestore.document("/notifications/{documentId}")
    .onCreate(async (snap, _) => {
        if(!snap) {
            console.log("No data associated with the event");
            return;
        }
        const data = snap.data();

        const payload = {
            notification: {
                title: data.title,
                body: data.message
            }
        }

        admin.messaging().sendToTopic(data.toTopic, payload).then((response) => {
            console.log('Successfully sent message:', response);
            return {success: true}
        }).catch((error) => {
            return {error: error.code}
        })
    })