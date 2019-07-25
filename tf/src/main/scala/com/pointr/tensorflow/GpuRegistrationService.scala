package com.pointr.tensorflow

import com.pointr.tensorflow.GpuClient.GpuInfo


trait GpuRegistrationService {
  def registerAlternate(host: String, port: Int): Option[TfClient]
  def gpus: Seq[GpuInfo]
  def broken: Seq[GpuInfo]
}
