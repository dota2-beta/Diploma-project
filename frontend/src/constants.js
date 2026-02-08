export const CATEGORIES = {
    themeScienceResearch: "Наука и исследования",
    themeAcademicProcess: "Учебный процесс",
    themeAcademicContests: "Олимпиады и конкурсы",
    themeExtracurricular: "Внеучебка и досуг",
    themeSport: "Спорт и ЗОЖ",
    themeCultureArt: "Культура и искусство",
    themeCareerEmployment: "Карьера и работа",
    themeAdministrationOfficial: "Официально / Ректорат",
    themePartnershipCollaboration: "Партнерство",
    themeCivicPatriotic: "Патриотизм",
    themeAdmissionCampaign: "Приёмная кампания",

    personStudents: "Для студентов",
    personAcademics: "Для преподавателей",
    personStaffAdmin: "Для сотрудников",
    personApplicants: "Для абитуриентов",
    personAlumni: "Для выпускников",
    personGeneral: "Общее / Для всех"
};

export const THEME_KEYS = Object.keys(CATEGORIES).filter(k => k.startsWith('theme'));
export const PERSON_KEYS = Object.keys(CATEGORIES).filter(k => k.startsWith('person'));