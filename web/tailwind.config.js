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
        primary: '#1A1A1A',
        ink: '#1A1A1A',
        'ink-light': '#3D3D3D',
        background: '#FFFFFF',
        paper: '#FAFAF8',
        surface: '#F7F6F3',
        divider: '#EDEBE6',
        'divider-heavy': '#DDDAD3',
        'text-primary': '#1A1A1A',
        'text-secondary': '#6B6B6B',
        'text-tertiary': '#9A9A9A',
        'text-hint': '#BFBFBF',
        danger: '#8B2500',
        cinnabar: '#8B2500',
        'cinnabar-light': '#B83A15',
        seal: '#C23A2B',
        gold: '#8B7355',
        'gold-light': '#C9B896',
        jade: '#3D6B4C',
      },
      fontFamily: {
        serif: ['"Noto Serif SC"', '"Songti SC"', '"Source Han Serif SC"', 'serif'],
        sans: ['"Noto Sans SC"', '-apple-system', 'BlinkMacSystemFont', '"PingFang SC"', '"Segoe UI"', 'sans-serif'],
      },
      borderRadius: {
        'none': '0px',
        'sm': '2px',
        'DEFAULT': '4px',
        'md': '6px',
        'lg': '8px',
        'xl': '12px',
      },
      boxShadow: {
        'none': 'none',
      },
      animation: {
        'fade-in': 'fadeIn 0.3s ease-out',
        'slide-up': 'slideUp 0.3s ease-out',
      },
      keyframes: {
        fadeIn: {
          '0%': { opacity: '0' },
          '100%': { opacity: '1' },
        },
        slideUp: {
          '0%': { transform: 'translateY(20px)', opacity: '0' },
          '100%': { transform: 'translateY(0)', opacity: '1' },
        },
      }
    },
  },
  plugins: [],
};
