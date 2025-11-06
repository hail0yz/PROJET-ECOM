import { Injectable } from '@angular/core';
import { Category } from '@/app/core/models/category.model';

@Injectable({ providedIn: 'root' })
export class CategoriesService {
    getCategories(): Category[] {
        return [
            /*{
                id: "1",
                name: "Fiction",
                description: "Romans contemporains et classiques de fiction.",
                image: "https://placehold.co/600x600?text='categorie 1'"
            },
            {
                id: "2",
                name: "Non-fiction",
                description: "Essais, témoignages et ouvrages documentaires.",
                image: "https://placehold.co/600x600?text='categorie 2'"
            },
            {
                id: "3",
                name: "Science",
                description: "Livres de vulgarisation et ouvrages scientifiques.",
                image: "https://placehold.co/600x600?text='categorie 3'"
            },
            {
                id: "4",
                name: "Histoire",
                description: "Histoire mondiale, biographies et chroniques.",
                image: "https://placehold.co/600x600?text='categorie 4'"
            },
            {
                id: "5",
                name: "Philosophie",
                description: "Textes philosophiques et pensées contemporaines.",
                image: "https://placehold.co/600x600?text='categorie 5'"
            },
            {
                id: "6",
                name: "Psychologie",
                description: "Études, guides et ouvrages sur le comportement humain.",
                image: "https://placehold.co/600x600?text='categorie 6'"
            },
            {
                id: "7",
                name: "Économie",
                description: "Théories économiques, finance et gestion.",
                image: "https://placehold.co/600x600?text='categorie 7'"
            },
            {
                id: "8",
                name: "Politique",
                description: "Analyses politiques, géopolitique et débats publics.",
                image: "https://placehold.co/600x600?text='categorie 8'"
            },
            {
                id: "9",
                name: "Art & Design",
                description: "Beaux-arts, design graphique et inspiration créative.",
                image: "https://placehold.co/600x600?text='categorie 9'"
            },
            {
                id: "10",
                name: "Musique",
                description: "Histoire de la musique, méthodes et biographies d'artistes.",
                image: "https://placehold.co/600x600?text='categorie 10'"
            },
            {
                id: "11",
                name: "Fantasy",
                description: "Récits épiques, mondes imaginaires et quêtes héroïques.",
                image: "https://placehold.co/600x600?text='categorie 11'"
            },
            {
                id: "12",
                name: "Science-fiction",
                description: "Futur, space-opera et récits technologiques.",
                image: "https://placehold.co/600x600?text='categorie 12'"
            },
            {
                id: "13",
                name: "Policier / Thriller",
                description: "Enquêtes, suspense et récits à rebondissements.",
                image: "https://placehold.co/600x600?text='categorie 13'"
            },
            {
                id: "14",
                name: "Romance",
                description: "Romans d'amour, contemporains et historiques.",
                image: "https://placehold.co/600x600?text='categorie 14'"
            },
            {
                id: "15",
                name: "Jeunesse",
                description: "Livres pour enfants et adolescents, albums et romans jeunesse.",
                image: "https://placehold.co/600x600?text='categorie 15'"
            },
            {
                id: "16",
                name: "Bande dessinée / Manga",
                description: "BD européennes, comics et mangas.",
                image: "https://placehold.co/600x600?text='categorie 16'"
            },
            {
                id: "17",
                name: "Développement personnel",
                description: "Guides pour progresser personnellement et professionnellement.",
                image: "https://placehold.co/600x600?text='categorie 17'"
            },
            {
                id: "18",
                name: "Programmation",
                description: "Livres techniques sur le développement logiciel et langages.",
                image: "https://placehold.co/600x600?text='categorie 18'"
            },
            {
                id: "19",
                name: "Intelligence artificielle",
                description: "Ouvrages sur IA, machine learning et data science.",
                image: "https://placehold.co/600x600?text='categorie 19'"
            },
            {
                id: "20",
                name: "Cuisine",
                description: "Recettes, techniques culinaires et gastronomie.",
                image: "https://placehold.co/600x600?text='categorie 20'"
            },
            {
                id: "21",
                name: "Voyages",
                description: "Récits de voyages, guides et inspirations pour partir.",
                image: "https://placehold.co/600x600?text='categorie 21'"
            },
            {
                id: "22",
                name: "Sport",
                description: "Biographies d'athlètes, entraînement et histoire du sport.",
                image: "https://placehold.co/600x600?text='categorie 22'"
            },
            {
                id: "23",
                name: "Environnement",
                description: "Climat, écologie et développement durable.",
                image: "https://placehold.co/600x600?text='categorie 23'"
            },
            {
                id: "24",
                name: "Langues",
                description: "Méthodes d'apprentissage des langues et linguistique.",
                image: "https://placehold.co/600x600?text='categorie 24'"
            }*/
        ]
            ;
    }
}
