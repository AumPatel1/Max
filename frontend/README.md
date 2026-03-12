# Max Frontend - Stock Exchange UI

Modern, responsive frontend for the Max stock exchange platform built with React, TypeScript, and Tailwind CSS.

## 🏗️ Project Structure

```
frontend/
├── public/              # Static assets
│   └── vite.svg
├── src/
│   ├── components/      # Reusable UI components
│   │   ├── Category/
│   │   │   └── CategoryBar.tsx
│   │   ├── Header/
│   │   │   ├── Header.tsx
│   │   │   ├── MaxLogo.tsx
│   │   │   └── UserAuth.tsx
│   │   └── Search/
│   │       ├── MobileSearch.tsx
│   │       └── SearchBar.tsx
│   ├── pages/          # Application pages
│   │   └── Home.tsx
│   ├── App.tsx         # Main app component
│   ├── main.tsx        # Application entry point
│   ├── index.css       # Global styles
│   └── App.css         # App-specific styles
├── index.html          # HTML template
├── package.json        # Dependencies and scripts
├── vite.config.ts      # Vite configuration
└── tsconfig.json       # TypeScript configuration
```

## 🛠️ Technology Stack

- **Framework**: React 19.2.0
- **Language**: TypeScript 5.9.3
- **Build Tool**: Vite 7.3.1
- **Styling**: Tailwind CSS 4.2.1
- **Routing**: React Router DOM 7.13.1
- **Linting**: ESLint 9.39.1

## 📋 Prerequisites

- Node.js v18 or higher
- npm v9 or higher (or yarn/pnpm)

## 🚀 Getting Started

### 1. Install Dependencies

```bash
cd frontend
npm install
```

### 2. Start Development Server

```bash
npm run dev
```

The application will start on `http://localhost:5173`

### 3. Build for Production

```bash
npm run build
```

Build output will be in the `dist/` directory.

### 4. Preview Production Build

```bash
npm run preview
```

## 📜 Available Scripts

| Script | Description |
|--------|-------------|
| `npm run dev` | Start development server with hot reload |
| `npm run build` | Build for production |
| `npm run preview` | Preview production build locally |
| `npm run lint` | Run ESLint to check code quality |

## 🎨 UI Components

### Header Components

#### MaxLogo
- Responsive logo with different sizes for mobile, tablet, and desktop
- Sizes: `text-3xl` (mobile), `text-4xl` (tablet), `text-5xl` (desktop)

#### UserAuth
- Login and Sign up buttons
- Responsive padding and text sizing
- Fixed height for consistency across devices
- Search button for mobile/tablet views

#### SearchBar
- Desktop search input with animated icon
- Shake animation on focus
- Color changes from gray to `#48CAE4` when focused

### Search Components

#### MobileSearch
- Full-screen search modal for mobile/tablet
- Blur background overlay
- Animated search icon
- Auto-focus input field
- Close button with X icon

### Category Components

#### CategoryBar
- Horizontal scrolling category navigation
- Mouse wheel horizontal scrolling support
- Smooth scroll behavior
- Categories: Trending, Politics, Sports, Culture, Crypto, Climate, Economics, Mentions, Companies, Financials, Tech & Science

## 🎨 Styling

### Tailwind CSS Configuration

The project uses Tailwind CSS 4 with custom utilities:

```css
/* Custom Scrollbar Hide */
.scrollbar-hide::-webkit-scrollbar {
  display: none;
}
.scrollbar-hide {
  -ms-overflow-style: none;
  scrollbar-width: none;
}

/* Custom Shake Animation */
@keyframes shake {
  0%, 100% { transform: rotate(0deg); }
  25% { transform: rotate(15deg); }
  75% { transform: rotate(-15deg); }
}
.animate-shake {
  animation: shake 0.5s ease-in-out;
}
```

### Color Palette

- **Primary**: `#48CAE4` (Cyan)
- **Background**: `#000000` (Black)
- **Secondary Background**: `#242423` (Dark Gray)
- **Text**: `#ffffff` (White)
- **Text Secondary**: `rgba(255, 255, 255, 0.6)` (White 60%)

### Responsive Breakpoints

- **Mobile**: < 640px (default)
- **Tablet**: ≥ 640px (`sm:`)
- **Desktop**: ≥ 768px (`md:`)

## 🔧 Development

### Component Development

Create new components in `src/components/`:

```tsx
// Example component
export default function MyComponent() {
  return (
    <div className="flex items-center gap-2">
      {/* Component content */}
    </div>
  );
}
```

### Adding New Pages

1. Create page in `src/pages/`
2. Add route in `App.tsx`:

```tsx
<Route path="/new-page" element={<NewPage />} />
```

### Custom Hooks

Create custom hooks in `src/hooks/` (create directory if needed):

```tsx
export function useCustomHook() {
  // Hook logic
}
```

## 🎯 Features

### Responsive Design
- Mobile-first approach
- Adaptive layouts for all screen sizes
- Touch-friendly interactions

### Animations
- Smooth transitions
- Shake effects on focus
- Scale animations on button clicks
- Blur backgrounds for modals

### User Experience
- Auto-focus on search inputs
- Keyboard navigation support
- Mouse wheel horizontal scrolling
- Visual feedback on interactions

## 🐛 Debugging

### Enable React DevTools
Install React Developer Tools browser extension

### TypeScript Errors
```bash
# Check TypeScript errors
npx tsc --noEmit
```

### Vite Issues
```bash
# Clear Vite cache
rm -rf node_modules/.vite
npm run dev
```

## 📦 Building

### Development Build
```bash
npm run dev
```

### Production Build
```bash
npm run build
```

### Analyze Bundle Size
```bash
npm run build -- --mode analyze
```

## 🚀 Deployment

### Deploy to Vercel
```bash
npm install -g vercel
vercel
```

### Deploy to Netlify
```bash
npm run build
# Upload dist/ folder to Netlify
```

### Environment Variables
Create `.env` file:
```env
VITE_API_URL=http://localhost:8080
VITE_APP_NAME=Max
```

Access in code:
```tsx
const apiUrl = import.meta.env.VITE_API_URL;
```

## 🧪 Testing (Future)

### Setup Testing
```bash
npm install -D vitest @testing-library/react @testing-library/jest-dom
```

### Run Tests
```bash
npm run test
```

## 📱 Mobile Optimization

- Bottom navigation for mobile devices
- Touch-optimized button sizes
- Responsive font sizes
- Optimized images and assets

## ♿ Accessibility

- Semantic HTML elements
- ARIA labels where needed
- Keyboard navigation support
- Focus indicators
- Color contrast compliance

## 🔒 Security

- No sensitive data in client-side code
- Environment variables for API endpoints
- Input sanitization
- XSS protection

## 📈 Performance

- Code splitting with React.lazy
- Optimized images
- Minimal bundle size
- Fast initial load time

## 🎨 Design System

### Typography
- Font: System fonts for optimal performance
- Sizes: Responsive with Tailwind utilities

### Spacing
- Consistent padding and margins
- Gap utilities for flex/grid layouts

### Components
- Reusable and composable
- Props for customization
- TypeScript for type safety

## 🤝 Contributing

1. Follow the existing code style
2. Use TypeScript for type safety
3. Write responsive components
4. Test on multiple devices
5. Use meaningful commit messages

## 📚 Resources

- [React Documentation](https://react.dev/)
- [TypeScript Documentation](https://www.typescriptlang.org/)
- [Tailwind CSS Documentation](https://tailwindcss.com/)
- [Vite Documentation](https://vitejs.dev/)
- [React Router Documentation](https://reactrouter.com/)

## 🐞 Common Issues

### Port Already in Use
```bash
# Change port
npm run dev -- --port 3000
```

### Module Not Found
```bash
# Reinstall dependencies
rm -rf node_modules package-lock.json
npm install
```

### TypeScript Errors
```bash
# Update TypeScript
npm install -D typescript@latest
```

## 📄 License

This project is private and proprietary.

## 👥 Authors

- Aum Patel
- Ved Patel

## 🙏 Acknowledgments

- React team for the amazing framework
- Tailwind CSS for the utility-first approach
- Vite for the blazing fast build tool
