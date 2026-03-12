# Max - Stock Exchange Platform

Max is a modern stock exchange platform built with a Spring Boot backend and React frontend. The platform provides real-time trading capabilities with a sleek, responsive user interface.

## 🏗️ Project Structure

```
Max/
├── backend/           # Spring Boot backend services
│   ├── matching-engine/   # Order matching engine
│   └── common/           # Shared utilities and models
└── frontend/         # React + TypeScript frontend
    └── src/
        ├── components/   # Reusable UI components
        └── pages/       # Application pages
```

## 🚀 Features

- **Real-time Trading**: High-performance order matching engine
- **Responsive Design**: Mobile-first UI with tablet and desktop support
- **Modern UI/UX**: Built with React 19 and Tailwind CSS 4
- **Category Navigation**: Browse markets by categories (Trending, Politics, Sports, Crypto, etc.)
- **Search Functionality**: Advanced search with smooth animations
- **User Authentication**: Login and signup functionality

## 🛠️ Tech Stack

### Backend
- **Framework**: Spring Boot 3.2.2
- **Language**: Java
- **Build Tool**: Maven
- **Key Dependencies**:
  - Spring Web
  - Spring Validation
  - Lombok

### Frontend
- **Framework**: React 19.2.0
- **Language**: TypeScript 5.9.3
- **Build Tool**: Vite 7.3.1
- **Styling**: Tailwind CSS 4.2.1
- **Routing**: React Router DOM 7.13.1

## 📋 Prerequisites

- **Java**: JDK 17 or higher
- **Node.js**: v18 or higher
- **npm**: v9 or higher
- **Maven**: 3.6 or higher

## 🚀 Quick Start

### 1. Clone the Repository
```bash
git clone <repository-url>
cd Max
```

### 2. Start Backend
```bash
cd backend/matching-engine
mvn clean install
mvn spring-boot:run
```

The backend will start on `http://localhost:8080`

### 3. Start Frontend
```bash
cd frontend
npm install
npm run dev
```

The frontend will start on `http://localhost:5173`

## 📚 Documentation

For detailed setup and development instructions, see:
- [Backend README](./backend/README.md)
- [Frontend README](./frontend/README.md)

## 🎨 UI Features

- **Responsive Header**: Adaptive navigation for mobile, tablet, and desktop
- **Category Bar**: Horizontal scrolling with mouse wheel support
- **Search Modal**: Animated search with blur background
- **Mobile Menu**: Bottom navigation for mobile devices
- **Custom Animations**: Shake effects and smooth transitions

## 🔧 Development

### Backend Development
```bash
cd backend/matching-engine
mvn spring-boot:run
```

### Frontend Development
```bash
cd frontend
npm run dev
```

### Build for Production

**Backend:**
```bash
cd backend/matching-engine
mvn clean package
```

**Frontend:**
```bash
cd frontend
npm run build
```

## 📝 Project Status

This project is currently in active development. Features are being added and refined regularly.

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is private and proprietary.

## 👥 Authors

- Aum Patel
- Ved Patel

## 🙏 Acknowledgments

- Spring Boot team for the excellent framework
- React team for the powerful UI library
- Tailwind CSS for the utility-first CSS framework
