import { BrowserRouter, Routes, Route, Link } from 'react-router-dom'
import Navbar from './components/Navbar'
import ProtectedRoute from './components/ProtectedRoute'

import Landing from './pages/Landing'
import Login from './pages/Login'
import Register from './pages/Register'
import NotFound from './pages/NotFound'
import ForgotPassword from './pages/ForgotPassword'
import ResetPassword from './pages/ResetPassword'
import VerifyEmail from './pages/VerifyEmail'
import Notifications from './pages/Notifications'
import JobDetails from './pages/JobDetails'
import FreelancerPublicProfile from './pages/FreelancerPublicProfile'
import Payments from './pages/Payments'
import AIDiagnosis from './pages/AIDiagnosis'
import EmergencyJobs from './pages/EmergencyJobs'
import { Terms, Privacy, Refunds } from './pages/Legal'

import ClientDashboard from './pages/client/ClientDashboard'
import PostProblem from './pages/client/PostProblem'
import ClientProblemDetails from './pages/client/ClientProblemDetails'
import ClientProfile from './pages/client/ClientProfile'
import FreelancerDirectory from './pages/client/FreelancerDirectory'

import FreelancerDashboard from './pages/freelancer/FreelancerDashboard'
import BrowseProblems from './pages/freelancer/BrowseProblems'
import FreelancerProblemDetail from './pages/freelancer/FreelancerProblemDetail'
import FreelancerProfile from './pages/freelancer/FreelancerProfile'
import MyProposals from './pages/freelancer/MyProposals'

import AdminDashboard from './pages/admin/AdminDashboard'
import AdminUsers from './pages/admin/AdminUsers'
import AdminProblems from './pages/admin/AdminProblems'
import AdminJobs from './pages/admin/AdminJobs'
import AdminDisputes from './pages/admin/AdminDisputes'

export default function App() {
  return (
    <>
      <Navbar />
      <main className="app-main">
        <Routes>
          <Route path="/" element={<Landing />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/forgot-password" element={<ForgotPassword />} />
          <Route path="/reset-password" element={<ResetPassword />} />
          <Route path="/verify-email" element={<VerifyEmail />} />
          <Route path="/freelancers/:userId" element={<FreelancerPublicProfile />} />

          <Route path="/notifications" element={<ProtectedRoute><Notifications /></ProtectedRoute>} />
          <Route path="/payments" element={<ProtectedRoute><Payments /></ProtectedRoute>} />
          <Route path="/ai-diagnosis" element={<ProtectedRoute><AIDiagnosis /></ProtectedRoute>} />
          <Route path="/terms" element={<Terms />} />
          <Route path="/privacy" element={<Privacy />} />
          <Route path="/refund-policy" element={<Refunds />} />
          <Route path="/freelancer/emergency" element={<ProtectedRoute roles={['FREELANCER']}><EmergencyJobs /></ProtectedRoute>} />
          <Route path="/jobs/:id" element={<ProtectedRoute><JobDetails /></ProtectedRoute>} />

          {/* Client */}
          <Route path="/client/dashboard" element={<ProtectedRoute roles={['CLIENT']}><ClientDashboard /></ProtectedRoute>} />
          <Route path="/client/post-problem" element={<ProtectedRoute roles={['CLIENT']}><PostProblem /></ProtectedRoute>} />
          <Route path="/client/problems/:id" element={<ProtectedRoute roles={['CLIENT','ADMIN']}><ClientProblemDetails /></ProtectedRoute>} />
          <Route path="/client/profile" element={<ProtectedRoute roles={['CLIENT']}><ClientProfile /></ProtectedRoute>} />
          <Route path="/client/freelancers" element={<ProtectedRoute roles={['CLIENT']}><FreelancerDirectory /></ProtectedRoute>} />

          {/* Freelancer */}
          <Route path="/freelancer/dashboard" element={<ProtectedRoute roles={['FREELANCER']}><FreelancerDashboard /></ProtectedRoute>} />
          <Route path="/freelancer/problems" element={<ProtectedRoute roles={['FREELANCER']}><BrowseProblems /></ProtectedRoute>} />
          <Route path="/freelancer/problems/:id" element={<ProtectedRoute roles={['FREELANCER']}><FreelancerProblemDetail /></ProtectedRoute>} />
          <Route path="/freelancer/proposals" element={<ProtectedRoute roles={['FREELANCER']}><MyProposals /></ProtectedRoute>} />
          <Route path="/freelancer/profile" element={<ProtectedRoute roles={['FREELANCER']}><FreelancerProfile /></ProtectedRoute>} />

          {/* Admin */}
          <Route path="/admin/dashboard" element={<ProtectedRoute roles={['ADMIN']}><AdminDashboard /></ProtectedRoute>} />
          <Route path="/admin/users" element={<ProtectedRoute roles={['ADMIN']}><AdminUsers /></ProtectedRoute>} />
          <Route path="/admin/problems" element={<ProtectedRoute roles={['ADMIN']}><AdminProblems /></ProtectedRoute>} />
          <Route path="/admin/jobs" element={<ProtectedRoute roles={['ADMIN']}><AdminJobs /></ProtectedRoute>} />
          <Route path="/admin/disputes" element={<ProtectedRoute roles={['ADMIN']}><AdminDisputes /></ProtectedRoute>} />

          <Route path="*" element={<NotFound />} />
        </Routes>
      </main>
      <footer className="footer">
        <div className="container">
          <span>© 2026 TechGarage — Your Software Repair Garage</span>
          <span className="mono" style={{ fontSize: 12 }}><Link to="/terms">Terms</Link> · <Link to="/privacy">Privacy</Link> · <Link to="/refund-policy">Refunds</Link></span>
        </div>
      </footer>
    </>
  )
}
