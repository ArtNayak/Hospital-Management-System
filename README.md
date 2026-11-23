# ART Medical Center: Hospital Management System

A desktop (Java Swing) application for running ART Medical Center: patient, doctor, staff and manager accounts, appointment booking and rescheduling, treatments and prescriptions, payments, feedback and reports. Data is stored in plain pipe-delimited text files, so there is no database to set up.

> Based on [Maneet7805/medical-center-java](https://github.com/Maneet7805/medical-center-java) (Maneet Arvind Mehta, Aubdool Shaad and contributors, originally a university course assignment). The full commit history of that project is kept here. The original repository has no license file, so check with its authors before reusing the code outside coursework.

## Features
- **Roles:** Admin, Manager, Staff, Doctor and Patient, each with its own dashboard.
- **Accounts:** registration with validation (phone, postcode, email, date of birth with age), login with a 3-attempt lockout, profile editing.
- **Appointments:** book with a doctor and time slot, reschedule, cancel (cancelled appointments are archived), view details, add treatments and medicines.
- **Payments and feedback:** pay for completed appointments, patients leave feedback for doctors, managers read it.
- **Management:** admin screens to add, update and delete users of every role.
- **Reports:** analytics report generator for the manager.

## Run it
You need **JDK 21** and **Maven**.

```
mvn compile exec:exec
```
This starts the app with the `data/` folder as its working directory. The data files (`doctors.txt`, `patients.txt`, ...) and `doctor_images/` live there, and the app finds them by relative name.

### Sample accounts
Every sample user has the password `123456`.

| Role | Username |
|---|---|
| Admin | `admin` |
| Manager | `oliviaa` |
| Staff | `amelia` |
| Doctor | `alex` |
| Patient | `stephie` |

The admin password can be changed with the environment variable `HMS_ADMIN_PASSWORD`. Set it before running anything real.

## Tests
```
mvn test                  # 15 unit tests: password hashing, user file handling, input validation
```
`src/test/java/assignment/GuiSmoke.java` is a manual check that opens every window once against the sample data (needs a display). The commands to run it are in the file's header comment.

## Project layout
```
src/main/java/assignment/    all application code (default package: assignment)
src/main/resources/assignment/   home and login background images
src/test/java/assignment/    unit tests and the GUI smoke check
data/                        sample data files and doctor_images/ (the app's working directory)
pom.xml                      Maven build (jdatepicker dependency, JUnit, run configuration)
```

## What was changed from the original
- **Renamed to ART Medical Center.** The window titles were changed, and the home and login background images, which had the old name baked into the picture, were edited to match.
- **It builds and runs now.** The original depended on a date-picker library (`jdatepicker`) that was not declared anywhere, so it did not compile. It also needed Java 21 while its CI used Java 11, and its CI called an Ant `build.xml` that did not exist. The project is now a Maven project on Java 21 with working CI.
- **The files are where the code looks for them.** The code opens `doctors.txt`, `doctor_images/` and others by bare name from the working directory, but the repo kept them in different folders. They are now together in `data/`.
- **Passwords are hashed** (salted PBKDF2). Before, they were stored and compared as plain text, and the admin password was hardcoded in the source. Hashing happens in one place, `UserFileHandler`, so every screen that edits users is covered. Old plain-text data is migrated automatically on first start, and the sample data ships already hashed. The user table and the edit dialog no longer show passwords, and leaving the password blank when editing keeps the current one.
- **Data files cannot be corrupted through form fields.** A `|` or line break in a username, address and so on used to add extra columns to a record. All forms now reject these characters.
- **New user IDs never collide.** They used to be random numbers with no check against existing users.
- **Safer file writes.** User files are written to a temporary file first and then swapped in, so a failure cannot leave a half-written file.
- **Edit dialog fix:** it chose doctor-only validation by looking for the word "doctor" in the window title. It now checks for the doctor-only fields.

## Known limitations
- Data is stored in text files read and written by one app instance. It is not safe for several people using it at once.
- Appointment, payment and treatment files are still plain text and are not validated as strictly as user data.
- The GUI has been opened and run against the sample data, but not every button has been exercised by hand.
