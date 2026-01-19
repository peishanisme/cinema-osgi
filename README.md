# 🎬 Cinema Booking System (OSGi + Apache Karaf)

This project is a **modular cinema booking system** built using **OSGi architecture** and deployed on **Apache Karaf**.  
Each business capability is implemented as an independent OSGi bundle, enabling loose coupling, scalability, and dynamic deployment.

The system is operated entirely through **Karaf shell commands**, simulating real-world cinema booking workflows such as user management, movie browsing, seat selection, booking, payment, and recommendations.

---

## System Architecture

The system is divided into the following OSGi bundles:

| Bundle Name                | Description                                          |
| -------------------------- | ---------------------------------------------------- |
| `cinema-api`               | Service interfaces and Data Models (POJOs)           |
| `cinema-commands`          | Gogo Shell command implementations                   |
| `db-component`             | Database connection management (PostgreSQL)          |
| `user-component`           | Managing all aspects of a user's account             |
| `movie-component`          | Movie metadata, sessions, and seat layout logic      |
| `booking-component`        | Handle booking, payment and ticket                   |
| `recommendation-component` | Manage recommendation for seats, movies and sessions |

---

## Technology Stack

- **Java JDK 17 or later**
- **Apache Maven 3.8+**
- **Apache Karaf 4.4+**
- **Git**
- **Supabase (PostgreSQL)**

## Install and Deployment Guide (Apache Karaf)

### 1. Setup Apache Karaf

1. **Open Terminal** (anywhere on your machine).
2. **Download Karaf**:
   ```powershell
   Invoke-WebRequest -Uri "[https://archive.apache.org/dist/karaf/4.4.9/apache-karaf-4.4.9.tar.gz](https://archive.apache.org/dist/karaf/4.4.9/apache-karaf-4.4.9.tar.gz)" -OutFile "apache-karaf-4.4.9.tar.gz" -UseBasicParsing
   ```
3. Extract the file:
   ```powershell
   tar -xzf apache-karaf-4.4.9.tar.gz
   ```
4. Navigate to your Karaf bin directory:
   ```powershell
   cd C:\path\to\your\apache-karaf-4.4.x\bin
   ```
5. Start Karaf:
   ```powershell
   .\karaf.bat
   ```
6. You should now see the Karaf logo and a prompt that looks like this:
   ```powershell
   karaf@root()>
   ```

### 2. Build the Project

1. Navigate to your project path in a standard terminal.
2. Run the build:
   ```powershell
    mvn clean install
   ```

### 3. Deploy Bundles to Karaf

Inside the Karaf console, run these commands in order:

    ```powershell
    # 1. Install Required SCR (for @Component)
    feature:install scr

    # 2. Install JDBC & Driver for Database
    feature:install jdbc
    bundle:install -s mvn:org.postgresql/postgresql/42.7.2

    # 3. Install Project Bundles
    bundle:install -s file:/C:/Users/user/cinema-osgi/cinema-api/target/cinema-api-1.0.0-SNAPSHOT.jar
    bundle:install -s file:/C:/Users/user/cinema-osgi/cinema-commands/target/cinema-commands-1.0.0-SNAPSHOT.jar
    bundle:install -s file:/C:/Users/user/cinema-osgi/db-component/target/db-component-1.0.0-SNAPSHOT.jar
    bundle:install -s file:/C:/Users/user/cinema-osgi/user-component/target/user-component-1.0.0-SNAPSHOT.jar
    bundle:install -s file:/C:/Users/user/cinema-osgi/movie-component/target/movie-component-1.0.0-SNAPSHOT.jar
    bundle:install -s file:/C:/Users/user/cinema-osgi/booking-component/target/booking-component-1.0.0-SNAPSHOT.jar
    bundle:install -s file:/C:/Users/user/cinema-osgi/recommendation-component/target/recommendation-component-1.0.0-SNAPSHOT.jar

Tips: After installation, verify if they are running by typing: list. You want to see Active in the status column. If you see Installed or Failure, check log:display.

## Command Reference

### 👤 User Account Management (UC1 – UC4)

| Description              | Command                                         | Example                                                                                 |
| ------------------------ | ----------------------------------------------- | --------------------------------------------------------------------------------------- |
| Register new user        | `cinema:register "name" "email" "password"`     | `cinema:register "tester" "tester@gmail.com" "123456789"`                               |
| Find user by email       | `cinema:user-find "email"`                      | `cinema:user-find "tester@gmail.com"`                                                   |
| Update user details      | `cinema:user-update [user_uuid] "name" "email"` | `cinema:user-update c39e7a3e-0131-4746-b617-638e9dc5e136 "tester1" "tester1@gmail.com"` |
| Update genre preferences | `cinema:genre-update [user_uuid] "genre"`       | `cinema:genre-update c39e7a3e-0131-4746-b617-638e9dc5e136 "Action"`                     |
| Add favourite movie      | `cinema:favourite-add [user_uuid] "movie_id"`   | `cinema:favourite-add c39e7a3e-0131-4746-b617-638e9dc5e136 "M002"`                      |
| View booking history     | `cinema:user-bookings [user_uuid]`              | `cinema:user-bookings c39e7a3e-0131-4746-b617-638e9dc5e136`                             |

### 🎬 Movie & Session Discovery (UC5 – UC13)

| Description               | Command                            | Example                          |
| ------------------------- | ---------------------------------- | -------------------------------- |
| List all movies           | `cinema:movie-list`                | `cinema:movie-list`              |
| Search movie by name      | `cinema:movie-search "keyword"`    | `cinema:movie-search "Avengers"` |
| Select movie              | `cinema:movie-select "movie_id"`   | `cinema:movie-select "M001"`     |
| Filter movies by genre    | `cinema:movie-filter "genre"`      | `cinema:movie-filter "Action"`   |
| List sessions for a movie | `cinema:movie-sessions "movie_id"` | `cinema:movie-sessions "M001"`   |
| Display seat map layout   | `cinema:seat-map "session_id"`     | `cinema:seat-map "S001"`         |

### 🎟️ Booking & Selection (UC16 – UC24)

| Description                  | Command                                                    | Example                                                              |
| ---------------------------- | ---------------------------------------------------------- | -------------------------------------------------------------------- |
| Select seat(s) for a session | `cinema:seat-select [user_uuid] [session_id] [seat_no...]` | `cinema:seat-select c39e7a3e-0131-4746-b617-638e9dc5e136 S001 40 41` |
| Create booking               | `cinema:book-create [user_uuid] [session_id] [seat_no...]` | `cinema:book-create c39e7a3e-0131-4746-b617-638e9dc5e136 S001 40 41` |
| Apply promo code             | `cinema:book-promo [booking_id] "promo_code"`              | `cinema:book-promo 7 "SAVE10"`                                       |
| Make payment                 | `cinema:book-pay [booking_id] "payment_method"`            | `cinema:book-pay 7 "Credit Card"`                                    |
| Generate QR ticket           | `cinema:book-ticket [booking_id]`                          | `cinema:book-ticket 7`                                               |
| Cancel booking               | `cinema:book-cancel [booking_id]`                          | `cinema:book-cancel 7`                                               |

### ✨ Recommendation Features (UC25 – UC28)

| Description                                | Command                                                 | Example                                                               |
| ------------------------------------------ | ------------------------------------------------------- | --------------------------------------------------------------------- |
| Recommend movies based on user preferences | `cinema:recommend-movies [user_id]`                     | `cinema:recommend-movies c39e7a3e-0131-4746-b617-638e9dc5e136`        |
| Auto-select recommended seat(s)            | `cinema:seat-select-rec [user_id] [session_id] [count]` | `cinema:seat-select-rec c39e7a3e-0131-4746-b617-638e9dc5e136 S001 2`  |
| Recommend sessions for a movie             | `cinema:session-recommend [movie_id]`                   | `cinema:session-recommend M001`                                       |
| View recommended movie details             | `cinema:view-recommended-movies [user_id]`              | `cinema:view-recommended-movies c39e7a3e-0131-4746-b617-638e9dc5e136` |

#### Supabase project: https://supabase.com/dashboard/project/lnuazfaxqcdykxsartsd
