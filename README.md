# SEMIS App

The **SEMIS** app is a  student-staff-school management suite developed in collaboration with Ministries of Education in the Global South. Tailored to meet core needs for enrolling and managing students and staff, SEMIS empowers staff to efficiently handle tasks like attendance tracking, promotion, repetition, transfers, graduation, and managing assessment data. The app supports both web-based and offline Android mobile data entry, ensuring accessibility even in low-connectivity regions.

<img width="208"  src="https://github.com/user-attachments/assets/ad6f2483-8076-48e6-867e-879d4e4afd50">

<img width="208"  src="https://github.com/user-attachments/assets/3d9f43b6-5fb9-4272-8ca7-cdace1076f18">

<img width="210"  src="https://github.com/user-attachments/assets/70fb7fe9-63de-40ea-bcff-c6a80392b2e5">


---

## ✨ New Features ✨

## General
1. **Sync Button Across All Screens**:
   - Add a sync button to each screen that allows users to manually synchronize the app with the online server, ensuring the latest data is available for both online and offline usage.
2. **Add Capture App Core Details**:
   - Implement the ability to show core details of the Capture App, to ensure compability with non SEMIS based data collections tools (programs and datasets).
3. **Save Last Filter Selection**:
   - Ensure that the app remembers the user's last filter settings (e.g: school, grade and class) so they can easily resume from where they left off without reselecting filters.
4. **Block Data Entry for Inactive Enrollments**:
   - Implement a feature that prevents data entry for any students marked as inactive, ensuring that only active enrollments are eligible for updates.

## Enrollment Module 

1. **View Student/Staff Details**:
   - Enhance the ability to view detailed profiles for both students and staff, the app redirects to the TEI Dashboard from the DHIS2 Android Capture App.

2. **Update Student/Staff Profile**:
   - Allow users to update student or staff profiles, including name, contact details, and other relevant personal data through the DHIS2 Capture App Interface.

## Attendance Module 
1. **Mark All as Absent or Present**:
   - Add a bulk action feature that enables users to mark all students in a class as either absent or present in one go, reducing the time required to manage attendance.


##  Attendance Follow-up 
A new module has been created to allow the absences follow-up:

1. **List Absences per Day**:
   - Add functionality to list all absences for a given day, allowing staff to quickly identify students who missed class.

2. **Review Absence Reasons and Additional Details**:
   - Add a review section where staff can document and update reasons for absences, providing an opportunity to add extra notes or follow-up details.


## Analytics 

1. **DHIS2 Capture App Analytics module enabled for SEMIS Use Case**


## 🛠️ Solved Issues 🛠️

- **Navigation and Data Saving Crashes:** Fixed issues that caused crashes during navigation and data saving in certain scenarios. The app is now more stable, ensuring better navigation and reliable data storage across all modules.


## Installation Files

Download the latest version of SEMIS from the following links:
- [SEMIS Android App](https://github.com/Saudigitus/dhis2-emis-android/releases/tag/v1.3)

---
To ensure the app functions smoothly without issues, please confirm that the web version of **SEMIS** is updated to at least **v1.3**. 
