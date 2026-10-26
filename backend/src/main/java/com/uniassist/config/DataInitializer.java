package com.uniassist.config;

import com.uniassist.entity.*;
import com.uniassist.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final FaqRepository faqRepository;
    private final AcademicCourseRepository courseRepository;
    private final AcademicEventRepository eventRepository;
    private final AnnouncementRepository announcementRepository;
    private final ModelVersionRepository modelVersionRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           FaqRepository faqRepository,
                           AcademicCourseRepository courseRepository,
                           AcademicEventRepository eventRepository,
                           AnnouncementRepository announcementRepository,
                           ModelVersionRepository modelVersionRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.faqRepository = faqRepository;
        this.courseRepository = courseRepository;
        this.eventRepository = eventRepository;
        this.announcementRepository = announcementRepository;
        this.modelVersionRepository = modelVersionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedUsers();
        seedFaqs();
        seedCourses();
        seedEvents();
        seedAnnouncements();
        seedModelVersion();
        logger.info("UniAssist database initialization completed successfully.");
    }

    private void seedUsers() {
        if (!userRepository.existsByEmail("admin@uniassist.edu")) {
            User admin = new User(
                    "System Administrator",
                    "ADMIN-001",
                    "admin@uniassist.edu",
                    "+91 9876543210",
                    "Administration",
                    "Staff",
                    0,
                    passwordEncoder.encode("Admin@123"),
                    "ROLE_ADMIN"
            );
            userRepository.save(admin);
            logger.info("Created default administrator: admin@uniassist.edu / Admin@123");
        }

        if (!userRepository.existsByEmail("student@uniassist.edu")) {
            User student = new User(
                    "Aarav Sharma",
                    "STU-2023-CSE042",
                    "student@uniassist.edu",
                    "+91 9123456780",
                    "Computer Science & Engineering",
                    "B.Tech",
                    4,
                    passwordEncoder.encode("Student@123"),
                    "ROLE_STUDENT"
            );
            userRepository.save(student);
            logger.info("Created demo student: student@uniassist.edu / Student@123");
        }
    }

    private void seedFaqs() {
        if (faqRepository.count() > 0) return;

        Object[][] faqsData = new Object[][]{
            {"How can I apply for undergraduate and postgraduate admissions?", "Admissions are conducted through the online admissions portal at https://admissions.university.edu. Candidates must fill the online application, upload academic marksheets, entrance scorecards (JEE/GATE/CAT), photo ID, and pay the registration fee before July 15.", "admissions", "apply admission application form register eligibility btech mca mba", "https://admissions.university.edu/guidelines"},
            {"What are the eligibility criteria for B.Tech admission?", "Candidates must pass 10+2 with minimum 60% aggregate in Physics, Mathematics, and Chemistry/Computer Science, and hold a valid JEE Main or University Entrance Test rank. 5% relaxation applies for SC/ST candidates.", "admissions", "btech eligibility criteria 10+2 marks jee cut off 60%", "https://admissions.university.edu/eligibility"},
            {"Can I get direct admission through lateral entry into 2nd year?", "Yes, candidates holding a 3-year AICTE-approved Diploma in Engineering or a B.Sc. with Mathematics (minimum 60% aggregate) can apply for Lateral Entry into the 3rd semester (2nd year) of B.Tech.", "admissions", "lateral entry direct admission diploma btech 2nd year", "https://admissions.university.edu/lateral-entry"},
            {"What is the mandatory attendance requirement to appear in examinations?", "Students must maintain at least 75% attendance in lectures, tutorials, and practicals in each subject. A 10% relaxation (down to 65%) can be condoned by the Dean on valid medical grounds or university-sponsored event participation with approved documentation.", "academics", "attendance percentage 75 requirement medical condonation debarred shortage", "https://academics.university.edu/ordinance/attendance"},
            {"Where can I find the official academic calendar for the current session?", "The official Academic Calendar is published biannually on the Academics page. The Autumn Semester runs August 1 to December 15, and the Spring Semester runs January 5 to May 20, detailing registration, exams, breaks, and sports fest.", "academics", "academic calendar semester dates schedule holidays autumn spring", "https://academics.university.edu/calendar"},
            {"How are class timetables and lab batches organized?", "Class timetables are arranged by department, semester, and section. Lectures run Monday through Friday from 9:00 AM to 4:30 PM with lunch from 1:00 PM to 2:00 PM. Lab batches alternate between practical sessions.", "academics", "class timetable lecture timings lab batch schedule periods", "https://academics.university.edu/timetables"},
            {"What are the graduation credit requirements for undergraduate programs?", "A regular 4-year B.Tech program requires successful completion of 160 credits across core, elective, humanities, and open courses. Honors or Minor specializations require an additional 18-20 credits.", "academics", "credits graduation requirements curriculum btech syllabus degree", "https://academics.university.edu/curriculum"},
            {"How do I submit course assignments and mini-projects?", "All course assignments must be submitted via the University Learning Management System (LMS) at https://lms.university.edu before 11:59 PM on the due date. Plagiarism exceeding 15% results in automatic rejection.", "academics", "assignments lms submission deadline plagiarism mini project homework", "https://lms.university.edu/submission-policy"},
            {"When are the mid-term and end-term examinations held?", "Mid-term assessments are conducted in the 8th week of the semester, while End-Term Examinations commence in the 16th week. Detailed date sheets are published 3 weeks in advance on the Examination portal.", "examinations", "exam schedule date sheet mid term end semester dates timings", "https://exams.university.edu/schedules"},
            {"How and when can I download my examination admit card / hall ticket?", "Admit cards are generated 7 days before examinations on the student ERP portal. Students with at least 75% attendance and no pending fee dues can download their verified hall ticket.", "examinations", "hall ticket admit card exam entry download erp portal", "https://exams.university.edu/admit-card"},
            {"When are semester results declared and how is CGPA calculated?", "Results are declared within 30-45 days following exams on https://results.university.edu. CGPA is the weighted average of grade points earned across all completed semesters on a 10-point scale. Passing grade is 'P' (Grade Point 4).", "examinations", "results declaration marksheet cgpa sgpa grade pass fail", "https://results.university.edu/rules"},
            {"What is the procedure for answer paper re-evaluation and verification?", "Students may apply for re-evaluation or obtain a photocopy of evaluated answer scripts within 10 days of result declaration via the Examination portal. The re-evaluation fee is Rs. 500 per theory paper.", "examinations", "re-evaluation rechecking answer script marks verification scrutiny", "https://exams.university.edu/reevaluation"},
            {"What are the payment options and deadlines for semester tuition fees?", "Tuition fees must be paid online through the Student Finance Portal using Net Banking, UPI, Debit/Credit Card, or NEFT/RTGS challan before the semester commencement date. A late fee of Rs. 100 per day applies afterwards.", "finance", "fee payment tuition fee installment deadline fine challan online", "https://finance.university.edu/payments"},
            {"What university and government scholarships are available for students?", "The university provides Merit Scholarships (up to 50% tuition waiver for top 5% branch rankers maintaining >= 8.5 CGPA), Merit-cum-Means assistance (family income < Rs. 3 Lakhs), and supports all National Scholarship Portal (NSP) schemes.", "finance", "scholarships merit concession financial aid waiver nsp ews", "https://welfare.university.edu/scholarships"},
            {"What are the operating hours and borrowing rules of the Central Library?", "The Central Library is open Monday to Saturday from 8:00 AM to 10:00 PM (open 24/7 during exam weeks). Undergraduates can borrow 4 books for 14 days with online renewal available on the library OPAC portal.", "campus", "library hours timings books issue return opac reading room journals", "https://library.university.edu/rules"},
            {"How do I apply for on-campus hostel accommodation?", "Hostel room allocation is processed online prior to each academic year via the Hostel Management Portal. Facilities include 2-seater and 3-seater furnished rooms with Wi-Fi, 24/7 power backup, and mess catering. In-campus curfew is 9:30 PM.", "campus", "hostel accommodation rooms mess timings curfew warden allotment", "https://hostel.university.edu/allotment"},
            {"Does the university operate campus bus transportation?", "Yes, the University Transport Division operates 45 air-conditioned buses across 30 city routes. Morning buses arrive at campus by 8:40 AM and evening departures are at 4:45 PM and 6:30 PM. Bus passes are issued per semester.", "campus", "bus transport route timings pass pickup points travel shuttle", "https://transport.university.edu/routes"},
            {"What medical and emergency healthcare facilities exist on campus?", "The University Health Center operates 24/7 with resident doctors, nursing staff, essential pharmaceuticals, and two emergency ambulances stationed at the health center (Emergency Ext: 4444).", "campus", "medical clinic health center doctor hospital ambulance emergency first aid", "https://campus.university.edu/health-center"},
            {"What sports and recreational amenities are provided for students?", "The campus features an Olympic-size swimming pool, 400m synthetic running track, football and cricket stadiums, 4 tennis courts, indoor badminton arena, and a multi-station gymnasium open 6:00 AM - 9:00 PM.", "campus", "sports gym swimming pool badminton cricket football fitness center", "https://campus.university.edu/sports"},
            {"How can I contact my course professors and departmental faculty?", "Faculty email addresses, cabin numbers, and research profiles are listed on the Faculty Directory page. Regular faculty consultation office hours are scheduled Tuesdays and Thursdays between 3:00 PM and 5:00 PM.", "support", "faculty contacts professor email cabin office hours appointment department", "https://academics.university.edu/faculty-directory"},
            {"Who should I contact for student portal login or campus Wi-Fi issues?", "For IT assistance, portal password resets, or Wi-Fi configuration (eduroam), visit the Computer Center Room 102 or email it-support@university.edu. You can also self-reset passwords via the login page.", "support", "technical support it helpdesk wifi password login portal erp eduroam", "https://it.university.edu/helpdesk"},
            {"How do I submit an official student grievance or support ticket?", "Students can submit formal support tickets directly from the Support page on this portal, categorizing their query (Academic, Finance, Hostel, Examination, Technical) to receive tracked staff responses within 48 hours.", "support", "support ticket grievance complaint inquiry help desk staff response", "https://support.university.edu/tickets"},
            {"What is the university grading scale and passing criteria?", "The university follows a 10-point relative grading scale: O (10), A+ (9), A (8), B+ (7), B (6), C (5), P (4 - Pass), and F (0 - Fail). Students must achieve a minimum of 40% aggregate in internal and external evaluations combined to pass.", "academics", "grading scale 10 point gpa cgpa marks percentage passing pass criteria", "https://exams.university.edu/grading-system"},
            {"Are banking and ATM services available on campus?", "Yes, a full-service State Bank branch with two 24/7 ATMs is located adjacent to the Student Activity Center, offering student savings accounts, education loan processing, and currency exchange.", "campus", "bank atm branch sbi cash student center finance deposit", "https://campus.university.edu/banking"},
            {"What is the official university policy regarding holidays and festival vacations?", "The university observes all national and state gazetted holidays. Semester mid-breaks include a 5-day Diwali break in autumn, a 2-week winter vacation in January, and a 1-week Holi break in spring. Emergency closures are notified via SMS/email.", "academics", "holidays vacation diwali winter break gazetted closed days festival", "https://academics.university.edu/holidays"}
        };

        for (Object[] row : faqsData) {
            Faq faq = new Faq(
                    (String) row[0],
                    (String) row[1],
                    (String) row[2],
                    (String) row[3],
                    (String) row[4],
                    true
            );
            faqRepository.save(faq);
        }
        logger.info("Seeded 25 university FAQs into knowledge base.");
    }

    private void seedCourses() {
        if (courseRepository.count() > 0) return;

        courseRepository.save(new AcademicCourse("CSE-BT4", "B.Tech in Computer Science & Engineering", "Computer Science & Engineering", 160, 8, "Accredited program emphasizing algorithms, artificial intelligence, software engineering, and cloud systems."));
        courseRepository.save(new AcademicCourse("IT-BT4", "B.Tech in Information Technology", "Information Technology", 160, 8, "Covers cybersecurity, networking, distributed computing, and full-stack enterprise development."));
        courseRepository.save(new AcademicCourse("ECE-BT4", "B.Tech in Electronics & Communication", "Electronics & Communication", 160, 8, "Specializations in VLSI, signal processing, embedded systems, and IoT."));
        courseRepository.save(new AcademicCourse("ME-BT4", "B.Tech in Mechanical Engineering", "Mechanical Engineering", 160, 8, "Focuses on robotics, thermodynamics, automotive design, and advanced manufacturing."));
        courseRepository.save(new AcademicCourse("MCA-PG2", "Master of Computer Applications (MCA)", "Computer Science & Engineering", 90, 4, "Advanced postgraduate degree for computing professionals, covering data science, ML, and enterprise software."));
        courseRepository.save(new AcademicCourse("MBA-PG2", "Master of Business Administration (MBA)", "Management Studies", 96, 4, "Dual specialization in Finance, Marketing, Human Resources, and Business Analytics."));
        logger.info("Seeded 6 academic degree programs.");
    }

    private void seedEvents() {
        if (eventRepository.count() > 0) return;

        eventRepository.save(new AcademicEvent("Autumn Semester Registration", "REGISTRATION", LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 5), "Course enrollment and fee clearance for all undergraduate and postgraduate students.", "Autumn 2026"));
        eventRepository.save(new AcademicEvent("Mid-Semester Examinations", "EXAMINATION", LocalDate.of(2026, 9, 21), LocalDate.of(2026, 9, 28), "Written assessments for continuous evaluation across all departments.", "Autumn 2026"));
        eventRepository.save(new AcademicEvent("Diwali & Autumn Mid-Break", "HOLIDAY", LocalDate.of(2026, 10, 28), LocalDate.of(2026, 11, 2), "University closed for institutional festival break.", "Autumn 2026"));
        eventRepository.save(new AcademicEvent("End-Semester Practical Examinations", "EXAMINATION", LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 27), "Laboratory evaluations and project demonstrations.", "Autumn 2026"));
        eventRepository.save(new AcademicEvent("End-Semester Theory Examinations", "EXAMINATION", LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 18), "Final comprehensive theory exams conducted by Controller of Examinations.", "Autumn 2026"));
        eventRepository.save(new AcademicEvent("Spring Semester Commencement", "ACADEMIC", LocalDate.of(2027, 1, 5), LocalDate.of(2027, 1, 6), "First teaching day of Spring 2027 semester.", "Spring 2027"));
        eventRepository.save(new AcademicEvent("Annual Cultural & Tech Fest (UniVision 2027)", "EVENT", LocalDate.of(2027, 2, 18), LocalDate.of(2027, 2, 21), "Pan-university technical competitions, hackathons, and cultural evenings.", "Spring 2027"));
        eventRepository.save(new AcademicEvent("Annual Convocation Ceremony", "CEREMONY", LocalDate.of(2027, 3, 12), LocalDate.of(2027, 3, 12), "Conferral of undergraduate, master and doctoral degrees.", "All"));
        logger.info("Seeded 8 academic calendar events.");
    }

    private void seedAnnouncements() {
        if (announcementRepository.count() > 0) return;

        announcementRepository.save(new Announcement("Mid-Term Examination Date Sheet Released", "The tentative date sheet for the upcoming mid-semester examinations has been published on the Examination portal. Verify timetable clashes by Friday.", "Examinations", LocalDate.of(2026, 11, 30)));
        announcementRepository.save(new Announcement("National Merit Scholarship Applications Open", "Eligible undergraduate students with CGPA >= 8.5 can submit their scholarship renewal forms to the Dean of Student Welfare Office before November 15.", "Scholarships", LocalDate.of(2026, 11, 15)));
        announcementRepository.save(new Announcement("Campus Placement Drive - Tech Giants Inc.", "Registration is open for final-year B.Tech and MCA students for upcoming software engineering placement interviews commencing next Monday.", "Placements", LocalDate.of(2026, 12, 31)));
        announcementRepository.save(new Announcement("Central Library Extended Hours During Exams", "Starting next week, the Central Library reading halls and digital learning lounge will remain open 24 hours daily through the examination period.", "Campus", LocalDate.of(2026, 12, 20)));
        announcementRepository.save(new Announcement("Annual Sports Meet Registrations Live", "Inter-departmental football, cricket, basketball, and athletics tournaments begin next month. Register your department teams online.", "Sports", LocalDate.of(2026, 11, 10)));
        logger.info("Seeded 5 campus announcements.");
    }

    private void seedModelVersion() {
        if (modelVersionRepository.count() > 0) return;

        modelVersionRepository.save(new ModelVersion(
                "UniAssist Intent Classifier",
                "v1.0.0",
                "models/model_pipeline.joblib",
                "Calibrated Linear SVC with TF-IDF (1,2 n-grams) achieving 70.73% test accuracy across 20 university intent classes.",
                "Chief AI Officer"
        ));
        logger.info("Seeded initial ML model version tracking entry.");
    }
}
