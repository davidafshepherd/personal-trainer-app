const {onDocumentCreated} = require("firebase-functions/v2/firestore");
const admin = require("firebase-admin");

admin.initializeApp();

exports.sendWorkoutScheduledPush = onDocumentCreated(
    "notificationJobs/{jobId}",
    async (event) => {
      const snapshot = event.data;

      if (!snapshot) {
        console.log("No snapshot data");
        return;
      }

      const job = snapshot.data();

      if (!job || job.type !== "WORKOUT_SCHEDULED") {
        return;
      }

      const traineeId = job.traineeId;

      if (!traineeId) {
        console.log("Missing traineeId", job);

        await snapshot.ref.update({
          pushSent: false,
          pushError: "Missing traineeId",
          processedAt: admin.firestore.FieldValue.serverTimestamp(),
        });

        return;
      }

      const traineeDoc = await admin
          .firestore()
          .collection("users")
          .doc(traineeId)
          .get();

      if (!traineeDoc.exists) {
        console.log("Trainee not found:", traineeId);

        await snapshot.ref.update({
          pushSent: false,
          pushError: "Trainee not found",
          processedAt: admin.firestore.FieldValue.serverTimestamp(),
        });

        return;
      }

      const trainee = traineeDoc.data();
      const fcmToken = trainee.fcmToken;

      if (!fcmToken) {
        console.log("No FCM token for trainee:", traineeId);

        await snapshot.ref.update({
          pushSent: false,
          pushError: "No FCM token for trainee",
          processedAt: admin.firestore.FieldValue.serverTimestamp(),
        });

        return;
      }

      const title = job.title || "Workout scheduled";
      const body = job.body || "Your trainer has scheduled a new workout.";

      const message = {
        token: fcmToken,
        data: {
          type: "WORKOUT_SCHEDULED",
          title: String(title),
          body: String(body),
          workoutName: String(job.workoutName || ""),
        },
        android: {
          priority: "high",
        },
      };

      try {
        const response = await admin.messaging().send(message);

        console.log("Push sent:", response);

        await snapshot.ref.update({
          pushSent: true,
          pushSentAt: admin.firestore.FieldValue.serverTimestamp(),
          pushMessageId: response,
        });
      } catch (error) {
        console.error("Failed to send push:", error);

        await snapshot.ref.update({
          pushSent: false,
          pushError: error.message,
          pushFailedAt: admin.firestore.FieldValue.serverTimestamp(),
        });
      }
    },
);
