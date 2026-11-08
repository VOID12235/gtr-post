export type Contest = {
  id: number
  title: string
  summary?: string
  cover?: string
  mode?: string
  status: string
  feeEnabled?: boolean
  feeAmount?: number
  signupStart?: string
  signupEnd?: string
  submitStart?: string
  submitEnd?: string
  reviewStart?: string
  reviewEnd?: string
  publishedAt?: string
}

export type Signup = {
  id: number
  contestId: number
  userId: number
  teamName?: string
  contact?: string
  status: string
  createdAt?: string
}

export type Work = {
  id: number
  contestId: number
  title: string
  description?: string
  videoUrl?: string
  fileUrl?: string
  status: string
  totalScore?: number
  authorNickname?: string
}

export type Sponsor = {
  id: number
  name: string
  industry?: string
  contactName?: string
  contactEmail?: string
  status: string
  createdAt?: string
}

export type Placement = {
  id: number
  slot: string
  contestId?: number
  weight?: number
}

export type Campaign = {
  id: number
  sponsorId: number
  sponsorName?: string
  tenantId?: number
  name: string
  type: string
  budget?: number
  status: string
  creativeText?: string
  creativeUrl?: string
  startAt?: string
  endAt?: string
  placements: Placement[]
}

export type PublicPlacement = {
  campaignId: number
  placementId: number
  slot: string
  contestId?: number
  sponsorName: string
  text: string
  url?: string
}

export type RubricItem = {
  id: number
  key: string
  label: string
  weight: number
  min: number
  max: number
  sortOrder: number
}

export type Rubric = {
  id: number
  contestId: number
  name: string
  mode: string
  status: string
  items: RubricItem[]
}

export type Certificate = {
  id: number
  contestId: number
  contestTitle?: string
  award: string
  code: string
  status: string
  issuedAt?: string
  holder?: string
}

export type Dashboard = {
  contests: number
  signups: number
  works: number
  certificates: number
  unreadNotifications: number
}

export type Notification = {
  id: number
  type: string
  title: string
  body?: string
  link?: string
  readAt?: string
  createdAt?: string
}

export type PaymentOrder = {
  id: number
  contestId: number
  userId: number
  amount: number
  status: string
  method: string
  reference?: string
  createdAt?: string
  paidAt?: string
}

export type UploadFile = {
  id: number
  originalName: string
  url: string
  size: number
  contentType?: string
}

export type PrizePool = {
  id?: number
  contestId: number
  totalAmount: number
  fundedAmount: number
  status: string
  note?: string
}

export type InsurancePolicy = {
  id?: number
  contestId: number
  type: string
  insurer?: string
  policyNo?: string
  coverage?: string
  status: string
}

export type SafetyPlan = {
  id?: number
  contestId: number
  level: string
  venue?: string
  capacity?: number
  plan?: string
  status: string
}

export type RiskView = {
  prize?: PrizePool
  insurances: InsurancePolicy[]
  safety?: SafetyPlan
}

export type Member = {
  id: number
  userId: number
  account: string
  nickname?: string
  email?: string
  role: string
  status: string
}

export type Product = {
  id: number
  name: string
  description?: string
  cover?: string
  price: number
  stock: number
  status: string
}

export type ShopOrder = {
  id: number
  productId: number
  userId: number
  quantity: number
  amount: number
  status: string
  contactName?: string
  contactPhone?: string
  address?: string
  note?: string
  trackingNo?: string
  createdAt?: string
  paidAt?: string
  shippedAt?: string
}
