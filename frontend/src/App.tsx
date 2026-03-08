import { Routes, Route } from "react-router-dom";
import Home from "./pages/Home";
import './App.css'

function App() {

  return (
    <Routes>
      <Route path="/" element={<Home />} />
      {/* <Route path="/market/:id" element={<Market />} /> */}
    </Routes>
  )
}

export default App
