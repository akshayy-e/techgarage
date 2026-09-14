import { Routes, Route, Link } from 'react-router-dom'
import { lazy, Suspense } from 'react'
import Navbar from './components/Navbar'
import ProtectedRoute from './components/ProtectedRoute'
import BackButton from './components/BackButton'

const Landing = lazy(() => import('./pages/Landing'))
const Login = lazy(() => import('./pages/Login'))
const Register = lazy(() => import('./pages/Register'))
const NotFound = lazy(() => import('./pages/NotFound'))
const ForgotPassword = lazy(() => import('./pages/ForgotPassword'))
const ResetPassword = lazy(() => import('./pages/ResetPassword'))
const Notifications = lazy(() => import('./pages/Notifications'))
const JobDetails = lazy(() => import('./pages/JobDetails'))
const FreelancerPublicProfile = lazy(() => import('./pages/FreelancerPublicProfile'))
const Payments = lazy(() => import('./pages/Payments'))
const AIDiagnosis = lazy(() => import('./pages/AIDiagnosis'))
const EmergencyJobs = lazy(() => import('./pages/EmergencyJobs'))
const Terms = lazy(() => import('./pages/Legal').then((m) => ({ default: m.Terms })))
const Privacy = lazy(() => import('./pages/Legal').then((m) => ({ default: m.Privacy })))
const Refunds = lazy(() => import('./pages/Legal').then((m) => ({ default: m.Refunds })))

const ClientDashboard = lazy(() => import('./pages/client/ClientDashboard'))
const PostProblem = lazy(() => import('./pages/client/PostProblem'))
const ClientProblemDetails = lazy(() => import('./pages/client/ClientProblemDetails'))
const ClientProfile = lazy(() => import('./pages/client/ClientProfile'))
const FreelancerDirectory = lazy(() => import('./pages/client/FreelancerDirectory'))

const FreelancerDashboard = lazy(() => import('./pages/freelancer/FreelancerDashboard'))
const BrowseProblems = lazy(() => import('./pages/freelancer/BrowseProblems'))
const FreelancerProblemDetail = lazy(() => import('./pages/freelancer/FreelancerProblemDetail'))
const FreelancerProfile = lazy(() => import('./pages/freelancer/FreelancerProfile'))
const MyProposals = lazy(() => import('./pages/freelancer/MyProposals'))

const AdminDashboard = lazy(() => import('./pages/admin/AdminDashboard'))
const AdminUsers = lazy(() => import('./pages/admin/AdminUsers'))
const AdminProblems = lazy(() => import('./pages/admin/AdminProblems'))
const AdminJobs = lazy(() => import('./pages/admin/AdminJobs'))
const AdminDisputes = lazy(() => import('./pages/admin/AdminDisputes'))

export default function App() {
  return (
    <>
      <Navbar />
      <main className="app-main">
        <BackButton />
        <Suspense fallback={<div className="route-loading" role="status"><span className="loading-spinner" />Loading TechGarage…</div>}>
        <Routes>
          <Route path="/" element={<Landing />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/forgot-password" element={<ForgotPassword />} />
          <Route path="/reset-password" element={<ResetPassword />} />
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
        </Suspense>
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
