/** @type {import('tailwindcss').Config} */
export default {
  darkMode: "class",
  content: ["./index.html", "./src/**/*.{js,ts,jsx,tsx}"],
  theme: {
    container: {
      center: true,
    },
    extend: {
      colors: {
        primary: '#000000',
        background: '#FFFFFF',
        surface: '#FAFAFA',
        'surface-variant': '#F5F5F5',
        divider: '#F0F0F0',
        'text-primary': '#1A1A1A',
        'text-secondary': '#666666',
        'text-tertiary': '#999999',
        'text-hint': '#CCCCCC',
        danger: '#FF2D55',
        gold: '#D4AF37',
        'gold-light': '#F5E6A3',
      },
      fontFamily: {
        sans: ['-apple-system', 'BlinkMacSystemFont', '"PingFang SC"', '"Noto Sans SC"', '"Segoe UI"', 'sans-serif'],
      },
      borderRadius: {
        'xl2': '16px',
        '2xl2': '20px',
      },
      animation: {
        'heart-pop': 'heartPop 0.6s ease-out forwards',
        'fade-in': 'fadeIn 0.3s ease-out',
        'slide-up': 'slideUp 0.3s ease-out',
        'coin-spin': 'coinSpin 0.8s ease-out',
        'shimmer': 'shimmer 2s infinite',
      },
      keyframes: {
        heartPop: {
          '0%': { transform: 'scale(0)', opacity: '0' },
          '40%': { transform: 'scale(1.3)', opacity: '1' },
          '70%': { transform: 'scale(1)', opacity: '1' },
          '100%': { transform: 'scale(0.8)', opacity: '0' },
        },
        fadeIn: {
          '0%': { opacity: '0' },
          '100%': { opacity: '1' },
        },
        slideUp: {
          '0%': { transform: 'translateY(20px)', opacity: '0' },
          '100%': { transform: 'translateY(0)', opacity: '1' },
        },
        coinSpin: {
          '0%': { transform: 'rotateY(0deg) scale(1)' },
          '50%': { transform: 'rotateY(360deg) scale(1.2)' },
          '100%': { transform: 'rotateY(720deg) scale(1)' },
        },
        shimmer: {
          '0%': { backgroundPosition: '-200% 0' },
          '100%': { backgroundPosition: '200% 0' },
        }
      }
    },
  },
  plugins: [],
};
