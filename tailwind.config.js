/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/main/resources/templates/**/*.html",
    "./src/main/resources/static/**/*.js" // if you have JS logic using tailwind classes
  ],
  theme: {
    extend: {},
  },
  plugins: [],
}